package com.miaosu.flux.recharge.task;

import com.miaosu.flux.locks.LockService;
import com.miaosu.flux.orders.Order;
import com.miaosu.flux.orders.OrderService;
import com.miaosu.flux.supplier.huazong.HuaZongPlatform;
import com.miaosu.flux.supplier.huazong.domain.GetOrderStatusResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 充值单号查询任务, 查询已发起过充值，但rechargeId为空的订单，重新向上游获取充值单号；
 * 主要场景：
 * 1. 订购结束后没有收到上游应答；
 * 2. 订购结束后回写id失败
 * Created by angus on 15/10/7.
 */
@Component
public class RechargeIdQueryTask implements DisposableBean {

    private static final String QUERY_RECHARGE_ID_LOCK = "query_recharge_id_lock";

    private static Logger logger = LoggerFactory.getLogger(RechargeIdQueryTask.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private LockService lockService;

    @Autowired
    private HuaZongPlatform huaZongPlatform;

    /**
     * 每5分钟运行一次
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void execute() {

        // 获取锁
        boolean locked = lockService.acquireLock(QUERY_RECHARGE_ID_LOCK);

        if (locked) {
            try {
                logger.debug("获取到充值单号查询锁");
                // 获取状态中、但充值id为空的订单
                List<Order> orderList = orderService.findUnknownRechargeIdOrders(200);

                for (final Order order : orderList) {
                    // 查询充值ID
                    getRechargeId(order);
                }
            } catch (Exception ex) {
                logger.error("RechargeIdQueryTask 发生异常", ex);
            } finally {
                lockService.releaseLock(QUERY_RECHARGE_ID_LOCK);
            }
        } else {
            logger.debug("未获取到充值单号查询锁，等待下次执行...");
        }

    }

    public void getRechargeId(final Order order) {
        try {
            logger.debug("查询{}的充值单号", order.getId());
            // 查询订单状态
            GetOrderStatusResult result = huaZongPlatform.queryOrderStatus(order.getId(), null);

            // 更新充值单号
            orderService.setRechargeId(order.getId(), result.getOrderNo(), "HZ");
        } catch (Exception ex) {
            logger.warn("{}订单充值单号查询失败", order.getId(), ex);
        }
    }


    @Override
    public void destroy() throws Exception {
        lockService.releaseLock(QUERY_RECHARGE_ID_LOCK);
    }
}
