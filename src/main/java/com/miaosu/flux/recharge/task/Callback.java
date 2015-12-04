package com.miaosu.flux.recharge.task;

import com.miaosu.flux.orders.enums.RechargeState;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 回调对象
 * Created by angus on 15/10/11.
 */
@Data
@AllArgsConstructor
public class Callback implements Serializable{
    private String orderId;

    private String transId;

    private String userName;

    private RechargeState rechargeState;

    private String notifyUrl;

    private String failedReason;
}
