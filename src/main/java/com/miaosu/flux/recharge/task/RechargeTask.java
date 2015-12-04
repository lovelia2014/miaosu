package com.miaosu.flux.recharge.task;

import com.miaosu.flux.locks.LockService;
import com.miaosu.flux.orders.Order;
import com.miaosu.flux.orders.OrderService;
import com.miaosu.flux.recharge.RechargeService;
import com.miaosu.flux.supplier.huazong.HuaZongPlatform;
import com.miaosu.flux.supplier.huazong.domain.OrderFlowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 充值任务, 查询一个小时内已付款未充值的订单
 * Created by angus on 15/10/7.
 */
@Component
public class RechargeTask implements DisposableBean {

    private static final String RECHARGE_LOCK = "recharge_lock";

    private static Logger logger = LoggerFactory.getLogger(RechargeTask.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private LockService lockService;

    @Autowired
    private RechargeService rechargeService;

    @Autowired
    private AsyncTaskExecutor asyncExecutor;

    @Autowired
    private HuaZongPlatform huaZongPlatform;

    @Scheduled(fixedDelay = 1000)
    public void execute() {

        // 获取锁
        boolean locked = lockService.acquireLock(RECHARGE_LOCK);

        if (locked) {
            try {
                logger.debug("获取到充值锁");
                // 获取未充值的订单
                List<Order> orderList = orderService.findUnRechargeOrders(200);

                for (final Order order : orderList) {
                    // 设置订单为充值中
                    if (setToRecharging(order.getId()) == 1) {
                        asyncExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                recharge(order);
                            }
                        });
                    }
                }
            } catch (Exception ex) {
                logger.error("RechargeTask 发生异常", ex);
            } finally {
                lockService.releaseLock(RECHARGE_LOCK);
            }
        } else {
            logger.debug("未获取到充值锁，等待下次执行...");
        }

    }

    public void recharge(final Order order) {
        try {
            logger.debug("发起对{}订单进行充值", order.getId());
            // 发起订购
            OrderFlowResult result = huaZongPlatform.order(order.getPhone(), order.getEffectType(), order.getProductId(),
                    order.getId(), order.getProvince());

            if ("Y".equals(result.getStatus())) {
                // 订购成功，更新充值单号
                orderService.setRechargeId(order.getId(), result.getOrderNo(), "HZ");
            } else if ("N".equals(result.getStatus())) { // 仅在明确返回失败时进行退款处理
                String failedReason = result.getFailedReason();
                //订购失败
                logger.info("{}充值失败，失败原因{}", order.getId(), failedReason);
                rechargeService.rechargeResultProcess(order.getId(), order.getUsername(), order.getNotifyUrl(), "N", failedReason, order.getExternalId());
            }
        } catch (Exception ex) {
            logger.warn("{}订单充值处理失败", order.getId(), ex);
        }

    }

    /**
     * 设置订单为充值中，只能将未充值的订单设置为充值中
     * @param orderId 订单编号
     * @return 更新行数
     */
    public int setToRecharging(final String orderId) {
        try {
            return orderService.setToRecharging(orderId);
        } catch (Exception ex) {
            logger.warn("设置订单{}为充值中失败，exMsg:{}", orderId, ex.getMessage());
        }
        return 0;
    }

    @Override
    public void destroy() throws Exception {
        lockService.releaseLock(RECHARGE_LOCK);
    }
}
