package com.miaosu.flux.orders;

import com.miaosu.flux.base.QueryResult;
import com.miaosu.flux.base.ResultCode;
import com.miaosu.flux.base.ResultInfo;
import com.miaosu.flux.base.ServiceException;
import com.miaosu.flux.orders.enums.PayState;
import com.miaosu.flux.orders.enums.RechargeState;
import com.miaosu.flux.recharge.RechargeService;
import com.miaosu.flux.recharge.task.RechargeStatusQueryTask;
import com.miaosu.flux.supplier.huazong.HuaZongPlatform;
import com.miaosu.flux.supplier.huazong.domain.GetOrderStatusResult;
import com.miaosu.flux.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 订单Controller
 * Created by angus on 15/10/2.
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private HuaZongPlatform huaZongPlatform;

    @Autowired
    private RechargeService rechargeService;

    @Autowired
    private RechargeStatusQueryTask rechargeStatusQueryTask;

    /**
     * 获取列表
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @Secured({"ROLE_USER", "ROLE_ADMIN", "ROLE_SYS_ADMIN"})
    public QueryResult<List<Order>> list(@RequestParam(value = "start", required = false) Integer start,
                                         @RequestParam(value = "limit", required = false) Integer size,
                                         @RequestParam(value = "id", required = false) String id,
                                         @RequestParam(value = "username", required = false) String username,
                                         @RequestParam(value = "externalId", required = false) String externalId,
                                         @RequestParam(value = "phone", required = false) String phone,
                                         @RequestParam(value = "effectType", required = false) Integer effectType,
                                         @RequestParam(value = "payState", required = false) Integer payState,
                                         @RequestParam(value = "rechargeState", required = false) Integer rechargeState,
                                         @RequestParam(value = "productId", required = false) String productId,
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(value = "begin", required = false) Date begin,
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(value = "end", required = false) Date end,
                                         Principal principal) {
        if (begin == null) {
            begin = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        }
        if (end == null) {
            end = new Date();
        }

        if (end.getTime() - begin.getTime() > 7 * 24 * 60 * 60 * 1000) {
            throw new ServiceException(ResultCode.DATA_CONSTRAINT_VIOLATION);
        }

        String currentUserName = principal.getName();
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();
        // 不允许非管理员用户查询其他用户信息
        if (!authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
                && !authorities.contains(new SimpleGrantedAuthority("ROLE_SYS_ADMIN"))
                && !currentUserName.equals(username)) {
            username = principal.getName();
//            throw new ServiceException(ResultCode.ACCESS_DENIED);
        }

        PayState pay_State = (payState == null ? null : PayState.forValue(payState));
        RechargeState recharge_State = (rechargeState == null ? null : RechargeState.forValue(rechargeState));

        Page<Order> products = orderService.find(id, username, externalId, begin, end, phone,
                effectType, pay_State, recharge_State, productId,
                PaginationUtil.generatePageRequest(start, size));
        return new QueryResult<>(products.getTotalElements(), products.getContent());
    }

    @RequestMapping(value = "/checkRechargeState", method = RequestMethod.POST)
    @Secured({"ROLE_ADMIN", "ROLE_SYS_ADMIN"})
    public ResultInfo checkRechargeState(@RequestParam("ids") String... ids) {
        List<Order> orders = orderService.find(ids);

        for (Order order : orders) {
            try {
                if (RechargeState.FAILED.equals(order.getRechargeState())) {
                    logger.info("支付失败的订单不需要核实状态，id: {}", order.getId());

                    // 保护措施，支付失败订单不需要核实状态
                    continue;
                }
                // 充值ID不存在时，先获取充值ID
                if (!StringUtils.hasText(order.getRechargeId())) {
                    try {
                        logger.debug("查询{}的充值单号", order.getId());
                        // 获取充值ID
                        GetOrderStatusResult result = huaZongPlatform.queryOrderStatus(order.getId(), null);

                        // 更新充值单号
                        orderService.setRechargeId(order.getId(), result.getOrderNo(), "HZ");
                    } catch (Exception ex) {
                        logger.warn("{}订单充值单号查询失败", order.getId(), ex);
                    }
                }

                rechargeStatusQueryTask.getRechargeStateAndProcess(order);
            } catch (Exception e) {
                logger.warn("查询{}充值状态发生异常, errMsg:{}", order.getId(), e.getMessage());
            }
        }
        return ResultInfo.SUCCESSFUL();
    }

    @RequestMapping(value = "/setToRechargeFailed", method = RequestMethod.POST)
    @Secured({"ROLE_ADMIN", "ROLE_SYS_ADMIN"})
    public ResultInfo setToRechargeFailed(@RequestParam("failedReason") String failedReason, @RequestParam("ids") String[] ids, Principal principal) {
        List<Order> orders = orderService.find(ids);
        for (Order order : orders) {
            try {
                if (RechargeState.SUCCESS.equals(order.getRechargeState())) {
                    logger.info("{}订单充值已成功，不能设置为失败", order.getId());

                    // 保护措施，已成功的订单不能设置为失败
                    continue;
                }
                rechargeService.rechargeResultProcess(order.getId(), order.getUsername(), order.getNotifyUrl(), "N",
                        "[" + principal.getName() + "]" + failedReason, order.getExternalId());
            } catch (Exception e) {
                logger.warn("手工设置订单为失败时异常, errMsg:{}", e.getMessage());
            }
        }
        return ResultInfo.SUCCESSFUL();
    }
}
