package com.miaosu.flux.supplier.huazong;

import com.miaosu.flux.base.ResultCode;
import com.miaosu.flux.base.ServiceException;
import com.miaosu.flux.orders.Order;
import com.miaosu.flux.orders.OrderService;
import com.miaosu.flux.recharge.RechargeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知处理
 * Created by angus on 15/10/7.
 */
@RestController
@RequestMapping("/notify")
public class HuaZongNotifyController {

    private static final Logger notifyLog = LoggerFactory.getLogger("notify");

    @Autowired
    private AsyncTaskExecutor executor;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RechargeService rechargeService;

    @Value("${huazong.secret}")
    private String secret = "";

    @RequestMapping(value = "/orderstatus", method = {RequestMethod.GET, RequestMethod.POST})
    public String orderStatus(@RequestParam(value = "Status", required = true) final String status, // 状态（Y:订购成功,N:订购失败)
                              @RequestParam(value = "FailedReason", required = true) final String failedReason, // 失败原因(订购成功时为空值)
                              @RequestParam(value = "OrderNo", required = true) final String rechargeId, // 充值订单号
                              @RequestParam(value = "Sign", required = true) String sign) {

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("Status", status);
        paramMap.put("FailedReason", failedReason);
        paramMap.put("OrderNo", rechargeId);

        // Step.1 签名校验
        if (sign == null || !sign.equals(HuaZongSign.getSign(paramMap, secret))) {
            throw new ServiceException(ResultCode.OPEN_SIGN_ERROR);
        }

        notifyLog.info("华众充值结果通知：{}", paramMap);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 根据充值单号查询三天内的订单信息
                    Order order = orderService.findByCreateTimeAfterAndRechargeId(new Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000), rechargeId);

                    if (order == null) {
                        notifyLog.warn("未找到充值单号为{}的订单", rechargeId);
                    } else {
                        rechargeService.rechargeResultProcess(order.getId(), order.getUsername(), order.getNotifyUrl(),
                                status, failedReason, order.getExternalId());
                    }
                } catch (Exception ex) {
                    notifyLog.warn("处理华众充值结果通知失败：{}， exMsg:{}", ex.getMessage());
                }
            }
        });

        return "ok";
    }
}
