package com.miaosu.flux.recharge.task;

import com.miaosu.flux.base.ResultCode;
import com.miaosu.flux.base.ServiceException;
import com.miaosu.flux.locks.LockService;
import com.miaosu.flux.orders.Order;
import com.miaosu.flux.orders.OrderService;
import com.miaosu.flux.recharge.RechargeService;
import com.miaosu.flux.supplier.huazong.HuaZongPlatform;
import com.miaosu.flux.supplier.huazong.domain.GetOrderStatusResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 充值状态查询任务；查询充值超过5分钟、小于30分钟依然没有回调的充值订单状态
 * Created by angus on 15/10/11.
 */
@Component
public class RechargeStatusQueryTask {

    private static final Logger logger = LoggerFactory.getLogger(RechargeStatusQueryTask.class);

    private static final String RECHARGE_STATUS_LOCK = "recharge_status_lock";

    @Autowired
    private AsyncTaskExecutor asyncExecutor;

    @Autowired
    private HuaZongPlatform huaZongPlatform;

    @Autowired
    private OrderService orderService;

    @Autowired
    private LockService lockService;

    @Autowired
    private RechargeService rechargeService;

    @Scheduled(fixedDelay = 10 * 1000)
    public void execute() {

        // 获取锁
        boolean locked = lockService.acquireLock(RECHARGE_STATUS_LOCK);

        if (locked) {
            int page = 0;
            boolean hasNext = false;
            try {
                do {
                    logger.debug("获取充值状态查询锁");
                    // 获取未充值的订单
                    Page<Order> orders = orderService.findUnknownRechargeStatusOrders(new PageRequest(page, 200));

                    for (final Order order : orders.getContent()) {
                        // 设置订单为充值中
                        asyncExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                getRechargeStateAndProcess(order);
                            }
                        });
                    }

                    if (orders.getTotalPages() - 1 > page) {
                        hasNext = true;
                    }
                    page++;
                } while (hasNext); // 取所有页
            } catch (Exception ex) {
                logger.error("RechargeStatusQueryTask 发生异常", ex);
            } finally {
                lockService.releaseLock(RECHARGE_STATUS_LOCK);
            }
        } else {
            logger.debug("未获取到充值状态查询锁，等待下次执行...");
        }
    }

    public void getRechargeStateAndProcess(Order order) {
        try {
            //订单状态（Y:订购成功,N:订购失败,P:订购中)
            GetOrderStatusResult result = huaZongPlatform.queryOrderStatus(order.getId(), order.getRechargeId());

            // 充值结果处理
            rechargeService.rechargeResultProcess(order.getId(), order.getUsername(), order.getNotifyUrl(), result.getStatus(),
                    result.getFailedReason(), order.getExternalId());
        } catch (ServiceException e) {
            if (ResultCode.DATA_NOT_EXISTS.equals(e.getErrorCode())) {
                logger.info("未查询到订单{}状态，订单不存在；执行充值失败逻辑", order.getId());
                // 订单不存在，说明上游没有收单成功，按失败逻辑处理
                rechargeService.rechargeResultProcess(order.getId(), order.getUsername(), order.getNotifyUrl(), "N", "-1: 上游未收单", order.getExternalId());
            }else {
                logger.warn("查询{}充值状态发生异常, errMsg:{}", order.getId(), e.getMessage());
            }
        } catch (Exception e) {
            logger.warn("查询{}充值状态发生异常, errMsg:{}", order.getId(), e.getMessage());
        }

    }

}
