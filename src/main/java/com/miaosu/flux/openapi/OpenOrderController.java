package com.miaosu.flux.openapi;

import com.miaosu.flux.base.ResultCode;
import com.miaosu.flux.base.ResultInfo;
import com.miaosu.flux.base.ServiceException;
import com.miaosu.flux.bills.AcctService;
import com.miaosu.flux.bills.Balance;
import com.miaosu.flux.bills.BalanceService;
import com.miaosu.flux.blacknums.BlackNumService;
import com.miaosu.flux.members.Member;
import com.miaosu.flux.members.MemberService;
import com.miaosu.flux.orders.Order;
import com.miaosu.flux.orders.OrderService;
import com.miaosu.flux.orders.enums.PayState;
import com.miaosu.flux.orders.enums.RechargeState;
import com.miaosu.flux.products.Product;
import com.miaosu.flux.products.ProductService;
import com.miaosu.flux.supplier.huazong.HuaZongPlatform;
import com.miaosu.flux.system.SerialNoUtil;
import com.miaosu.flux.system.users.User;
import com.miaosu.flux.system.users.UserService;
import com.miaosu.flux.util.DESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单开放接口
 * Created by angus on 15/10/4.
 */
@RestController
@RequestMapping("/openapi/order")
public class OpenOrderController extends OpenBaseController {
    private static Logger logger = LoggerFactory.getLogger(OpenOrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private SerialNoUtil serialNoUtil;

    @Autowired
    private AcctService acctService;

    @Autowired
    private HuaZongPlatform huaZongPlatform;

    @Autowired
    private BlackNumService blackNumService;

    @Autowired
    @Qualifier("orderRechargeStateCache")
    private Cache orderRechargeStateCache;

    @RequestMapping(value = "create", method = { RequestMethod.GET, RequestMethod.POST })
    public ResultInfo<Map<String, Object>> create(@RequestParam(value = "userId") final String userId,
            @RequestParam(value = "phone") final String phone,
            @RequestParam(value = "type", required = false) final Integer type,
            @RequestParam(value = "productId") final String productId,
            @RequestParam(value = "transId") final String transId,
            @RequestParam(value = "province") final String province,
            @RequestParam(value = "notifyUrl", required = false) final String notifyUrl,
            @RequestParam(value = "sign") final String sign) {
        Map<String, Object> data = new HashMap<>();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", userId);
        paramMap.put("phone", phone);
        paramMap.put("type", type);
        paramMap.put("productId", productId);
        paramMap.put("transId", transId);
        paramMap.put("province", province);
        paramMap.put("notifyUrl", notifyUrl);

        // Step.1 签名校验
        Member member = memberService.get(userId);
        User user = userService.get(userId);
        if (member == null || user == null || !user.isEnabled()) {
            // 用户不存在或被禁用
            throw new ServiceException(ResultCode.OPEN_USER_NOT_EXISTS);
        }
        checkSign(paramMap, sign, DESUtil.decryptToString(member.getToken(), userId));

        // Step.2 订单是否已存在
        Order order = orderService.findByUsernameAndExternalId(userId, transId);
        if (order != null) {
            // 幂等设计，订单存在时返回订单编号
            data.put("orderId", order.getId());
            return new ResultInfo<>(true, ResultCode.SUCCESSFUL, data);
        }

        // Step.3 商品校验
        Product product = productService.get(productId);
        if (product == null || !product.isEnabled()) {
            // 商品不存在或被禁用
            throw new ServiceException(ResultCode.OPEN_PRODUCT_NOT_EXISTS);
        }

        // 计算订单价格
        BigDecimal orderPrice = product.getPrice().multiply(member.getDiscount()).setScale(4, BigDecimal.ROUND_DOWN);

        // Step.4 余额校验
        Balance balance = balanceService.get(userId);
        if (balance == null || balance.getBalance().compareTo(orderPrice) < 0) {
            // 余额不足
            throw new ServiceException(ResultCode.OPEN_NO_BALANCE);
        }

        // Step.5 生成订单与支付流水
        String orderId = serialNoUtil.genrateOrderNo();
        String billId = serialNoUtil.genrateBillNo();
        if (orderId == null || billId == null) {
            logger.warn("生成订单号与流水单号失败");
            throw new ServiceException(ResultCode.FAILED);
        }

        Order newOrder = new Order();
        newOrder.setId(orderId);
        newOrder.setUsername(userId);
        newOrder.setEffectType(type);
        newOrder.setExternalId(transId);
        newOrder.setNotifyUrl(notifyUrl);
        newOrder.setPayId(billId);
        newOrder.setPayState(PayState.INIT);
        newOrder.setPhone(phone);
        newOrder.setPrice(orderPrice);
        newOrder.setProductId(productId);
        newOrder.setProductName(product.getName());
        newOrder.setProductPrice(product.getPrice());
        newOrder.setProvince(province);

        try {
            order = orderService.create(newOrder);
        } catch (ServiceException ex) {
            // 数据已存在
            if (ResultCode.DATA_EXISTS.equals(ex.getErrorCode())) {
                order = orderService.findByUsernameAndExternalId(userId, transId);
                if (order != null) {
                    // 幂等设计，订单存在时返回订单编号
                    data.put("orderId", order.getId());
                    return new ResultInfo<>(true, ResultCode.SUCCESSFUL, data);
                }
            } else {
                throw ex;
            }
        }
        if (order == null) {
            throw new ServiceException(ResultCode.FAILED);
        }

        // Step.6 支付订单
        try {
            //0：成功；1：余额不足；2: 支付单号不存在；3: 支付单状态异常
            int payState = acctService.payment(userId, billId, orderId);
            logger.info("订单{}与支付单{}的支付处理结果：{}", orderId, billId, payState);
        } catch (Exception ex) {
            logger.warn("下单时支付失败，orderId:{}, billId:{}", orderId, billId, ex);
        }

        data.put("orderId", order.getId());
        return new ResultInfo<>(true, ResultCode.SUCCESSFUL, data);
    }

    @RequestMapping(value = "validate", method = { RequestMethod.GET, RequestMethod.POST })
    public ResultInfo validate(@RequestParam(value = "userId") final String userId,
            @RequestParam(value = "phone") final String phone,
            @RequestParam(value = "productId") final String productId,
            @RequestParam(value = "sign") final String sign) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", userId);
        paramMap.put("phone", phone);
        paramMap.put("productId", productId);

        // Step.1 签名校验
        Member member = memberService.get(userId);
        User user = userService.get(userId);
        if (member == null || user == null || !user.isEnabled()) {
            // 用户不存在或被禁用
            throw new ServiceException(ResultCode.OPEN_USER_NOT_EXISTS);
        }
        checkSign(paramMap, sign, DESUtil.decryptToString(member.getToken(), userId));

        // Step.2 检验能否充值
        try {
            boolean result = huaZongPlatform.validate(phone, productId);
            return result ? ResultInfo.SUCCESSFUL() : ResultInfo.FAILED();
        }catch(Exception ex){
            logger.warn("{}校验{}能否充值失败, exMsg:{}", phone, productId, ex.getMessage());
            return new ResultInfo(false, ResultCode.FAILED, ex.getMessage());
        }
    }

    @RequestMapping(value = "status", method = { RequestMethod.GET, RequestMethod.POST })
    public ResultInfo<Map<String, Object>> status(@RequestParam(value = "userId") final String userId,
                                                  @RequestParam(value = "orderId") final String orderId,
                                                  @RequestParam(value = "sign") final String sign){
        Map<String, Object> data = new HashMap<>();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", userId);
        paramMap.put("orderId", orderId);

        // Step.1 签名校验
        Member member = memberService.get(userId);
        User user = userService.get(userId);
        if (member == null || user == null || !user.isEnabled()) {
            // 用户不存在或被禁用
            throw new ServiceException(ResultCode.OPEN_USER_NOT_EXISTS);
        }
        checkSign(paramMap, sign, DESUtil.decryptToString(member.getToken(), userId));

        // Step.2 查询订单信息
        RechargeState rechargeState = null;
        try {
            // 从缓存获取状态
            rechargeState = orderRechargeStateCache.get(orderId, RechargeState.class);

            if(rechargeState != null){
                logger.debug("查询{}充值状态命中缓存, {}", orderId, rechargeState);
                data.put("orderId", orderId);
                data.put("status", rechargeState);
                return new ResultInfo<>(true, ResultCode.SUCCESSFUL, data);
            }
        }catch (Exception e){
            logger.info("从缓存中获取{}充值状态失败, exMsg:{}", orderId, e.getMessage());
        }

        logger.debug("查询{}充值状态未命中缓存, {}", orderId, rechargeState);

        // 从数据库获取
        Order order = orderService.findByIdAndUsername(orderId, userId);

        if(order == null) {
            try {
                // 为null时也加入缓存，防止恶意刷
                orderRechargeStateCache.put(orderId, rechargeState);
            }catch (Exception e){
                logger.info("{}充值状态{}加入缓存失败, exMsg:{}", orderId, rechargeState, e.getMessage());
            }
            throw new ServiceException(ResultCode.OPEN_ORDER_NOT_EXISTS);
        }

        // 为空时返回充值中
        rechargeState = (order.getRechargeState() == null ? RechargeState.PROCESS : order.getRechargeState());

        try {
            // 加入缓存
            orderRechargeStateCache.put(orderId, rechargeState);
        }catch (Exception e){
            logger.info("{}充值状态{}加入缓存失败, exMsg:{}", orderId, rechargeState, e.getMessage());
        }

        data.put("orderId", order.getId());
        data.put("status", rechargeState);
        return new ResultInfo<>(true, ResultCode.SUCCESSFUL, data);
    }


    @RequestMapping(value = "queryOrderId", method = { RequestMethod.GET, RequestMethod.POST })
    public ResultInfo<Map<String, Object>> query(@RequestParam(value = "userId") final String userId,
                                                  @RequestParam(value = "transId") final String transId,
                                                  @RequestParam(value = "sign") final String sign) {
        Map<String, Object> data = new HashMap<>();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", userId);
        paramMap.put("transId", transId);

        // Step.1 签名校验
        Member member = memberService.get(userId);
        User user = userService.get(userId);
        if (member == null || user == null || !user.isEnabled()) {
            // 用户不存在或被禁用
            throw new ServiceException(ResultCode.OPEN_USER_NOT_EXISTS);
        }
        checkSign(paramMap, sign, DESUtil.decryptToString(member.getToken(), userId));

        Order order = orderService.findByUsernameAndExternalId(userId, transId);

        if(order == null){
            throw new ServiceException(ResultCode.OPEN_ORDER_NOT_EXISTS);
        }

        data.put("orderId", order.getId());
        return new ResultInfo<>(true, ResultCode.SUCCESSFUL, data);
    }
}
