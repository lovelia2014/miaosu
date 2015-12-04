package com.miaosu.flux.recharge;

import com.miaosu.flux.bills.AcctService;
import com.miaosu.flux.orders.OrderService;
import com.miaosu.flux.orders.enums.RechargeState;
import com.miaosu.flux.recharge.task.Callback;
import com.miaosu.flux.recharge.task.CallbackTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 充值服务
 * Created by angus on 15/10/16.
 */
@Service
public class RechargeService {

    private static final Logger logger = LoggerFactory.getLogger(RechargeService.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private AcctService acctService;

    public void rechargeResultProcess(String orderId, String userName, String notifyUrl, String status, String failedReason, String externalId) {
            try {
                if ("Y".equals(status)) {
                    orderService.rechargeSuccess(orderId);

                    // 回调地址不为空时，添加一条回调信息到队列
                    if (StringUtils.hasText(notifyUrl)) {
                        CallbackTask.QUEUE.put(new Callback(orderId, externalId, userName, RechargeState.SUCCESS, notifyUrl, failedReason));
                    }
                } else if ("N".equals(status)) {
                    this.rechargeFailed(orderId, userName, notifyUrl, failedReason, externalId);
                }
            } catch (Exception e) {
                logger.warn("处理{}充值结果发生异常；status:{}, failedReason:{}, exMsg:{}", orderId, status, failedReason, e.getMessage());
            }
    }

    public void rechargeFailed(String orderId, String userName, String notifyUrl, String failedReason, String externalId){

        String subReason = failedReason != null && failedReason.length() > 1000 ? failedReason.substring(0, 1000) + "......" : failedReason;
        // 订购失败，更新订单失败原因、生成退款流水
        String refundBillId = orderService.rechargeFailed(orderId, subReason);

        // 退款
        logger.info("发起对{}订单进行退款，退款单号{}", orderId, refundBillId);
        acctService.refund(userName, refundBillId, orderId);

        // 回调地址不为空时，添加一条回调信息到队列
        if (StringUtils.hasText(notifyUrl)) {
            try {
                CallbackTask.QUEUE.put(new Callback(orderId, externalId, userName, RechargeState.FAILED, notifyUrl, failedReason));
            } catch (InterruptedException e) {
                logger.warn("加入回调消息到队列失败；orderId:{}, failedReason:{}, notifyUrl:{}", orderId, failedReason, notifyUrl, e);
            }
        }
    }
}
