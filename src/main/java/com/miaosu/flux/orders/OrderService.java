package com.miaosu.flux.orders;

import com.miaosu.flux.base.ResultCode;
import com.miaosu.flux.base.ServiceException;
import com.miaosu.flux.bills.Bill;
import com.miaosu.flux.bills.BillRepository;
import com.miaosu.flux.bills.enums.BillChannel;
import com.miaosu.flux.bills.enums.BillStatus;
import com.miaosu.flux.bills.enums.BillType;
import com.miaosu.flux.orders.enums.PayState;
import com.miaosu.flux.orders.enums.RechargeState;
import com.miaosu.flux.system.SerialNoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单Service
 * Created by angus on 15/10/2.
 */
@Service
@Transactional(readOnly = true, timeout = 10)
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private SerialNoUtil serialNoUtil;

    public Page<Order> find(String id, String username, String externalId, Date begin, Date end, String phone,
                            Integer effectType, PayState payState, RechargeState rechargeState, String productId, Pageable pageable) {
        if (begin == null) {
            begin = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        }
        if (end == null) {
            end = new Date();
        }

        return orderRepository.findByCondition(id, username, externalId, begin, end, phone, effectType, payState, rechargeState, productId, pageable);
    }

    /**
     * 根据用户名与外部订单号查询订单
     * @param username 用户名
     * @param externalId 外部订单号
     * @return 订单
     */
    public Order findByUsernameAndExternalId(String username, String externalId) {
        return orderRepository.findByUsernameAndExternalId(username, externalId);
    }

    /**
     * 根据ID查询订单
     * @param id 订单ID
     * @return 订单
     */
    public Order get(String id) {
        return orderRepository.findOne(id);
    }

    /**
     * 根据订单ID与用户名查询订单，防止用户越权查询订单
     * @param id 订单ID
     * @param userName 用户名
     * @return 订单
     */
    public Order findByIdAndUsername(String id, String userName) {
        return orderRepository.findByIdAndUsername(id, userName);
    }

    /**
     * 查询未充值的订单
     * @param maxOrders 获取数量
     * @return 订单列表
     */
    public List<Order> findUnRechargeOrders(int maxOrders) {
        return orderRepository.findUnRechargeOrders(maxOrders);
    }

    /**
     * 删除订单
     * @param ids id数组
     */
    @Modifying
    @Transactional
    public void remove(String... ids) {
        for (String id : ids) {
            orderRepository.delete(id);
        }
    }

    /**
     * 创建订单，并生成支付流水
     * @param order 订单
     * @return 订单
     */
    @Modifying
    @Transactional
    public Order create(Order order) {
        try {
            Order result = orderRepository.save(order);
            billRepository.save(new Bill(order.getPayId(), order.getUsername(), order.getPrice().negate(), null,
                    BillType.SUBTRACTION, BillChannel.PAYMENT, order.getId() + "-订单支付", BillStatus.INIT, new Date()));
            return result;
        } catch (ConstraintViolationException ex) {
            throw new ServiceException(ResultCode.DATA_EXISTS);
        }
    }

    /**
     * 充值失败，更新订单充值结果，并生成退款流水
     * @param orderId 订单编号
     * @param failedReason 失败原因
     * @return 退款流水ID
     */
    @Modifying
    @Transactional
    public String rechargeFailed(final String orderId, final String failedReason) {
        String billId = serialNoUtil.genrateBillNo();

        // 更新充值结果
        int rows = orderDao.rechargeFailed(orderId, failedReason, billId);

        // 幂等，一个订单只能生成一个退款流水
        if(rows == 1) {
            Order order = orderRepository.findOne(orderId);
            // 生成退款流水
            billRepository.save(new Bill(billId, order.getUsername(), order.getPrice(), null, BillType.ADD,
                    BillChannel.REFUND, orderId + "-订单退款", BillStatus.INIT, new Date()));
            return billId;
        }
        return null;
    }

    /**
     * 充值成功
     * @param orderId 订单编号
     */
    @Modifying
    @Transactional
    public void rechargeSuccess(final String orderId) {
        orderDao.rechargeSuccess(orderId);
    }

    /**
     * 设置订单为充值中，只能将未充值的订单设置为充值中
     * @param orderId 订单编号
     * @return 更新行数
     */
    @Transactional
    public int setToRecharging(String orderId) {
        return orderDao.setToRecharging(orderId);
    }

    /**
     * 设置充值单号
     * @param id 订单ID
     * @param rechargeId 充值单号
     * @param rechargeSystem 充值系统
     */
    @Modifying
    @Transactional
    public void setRechargeId(String id, String rechargeId, String rechargeSystem) {
        orderRepository.setRechargeId(id, rechargeId, rechargeSystem);
    }

    /**
     * 根据充值ID查询指定时间之后的订单
     * @param begin 起始时间
     * @param rechargeId 充值ID
     */
    public Order findByCreateTimeAfterAndRechargeId(Date begin, String rechargeId) {
        return orderRepository.findByCreateTimeAfterAndRechargeId(begin, rechargeId);
    }

    /**
     * 查询5分钟前，60分钟内的充值中的订单
     * @param pageable 分页信息
     * @return 订单列表
     */
    public Page<Order> findUnknownRechargeStatusOrders(Pageable pageable) {
        Date now = new Date();
        return orderRepository.findUnknownRechargeStatusOrders(new Date(now.getTime() - 60 * 60 * 1000),
                new Date(now.getTime() - 5 * 60 * 1000), pageable);
    }

    /**
     * 查询1小时内，支付成功、且充值状态大于等于充值中，但充值ID为空的订单
     * @param maxOrders 最大订单数
     * @return 订单列表
     */
    public List<Order> findUnknownRechargeIdOrders(int maxOrders) {
        return orderRepository.findUnknownRechargeIdOrders(maxOrders);
    }

    /**
     * 根据指定的id查询订单
     * @param ids id数组
     * @return 订单列表
     */
    public List<Order> find(String... ids) {
        List<Order> orders = new ArrayList<>();

        for (String id : ids) {
            orders.add(orderRepository.findOne(id));
        }

        return orders;
    }
}
