package com.miaosu.flux.bills.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 流水来源（0：充值；1：支付；2：退款；3：奖励；4：其他；）
 * Created by angus on 15/10/4.
 */
public enum BillChannel {
    RECHARGE, PAYMENT, REFUND, REWARD, OTHERS;

    @JsonCreator
    public static BillChannel forValue(int ordinal) {
        return BillChannel.values()[ordinal];
    }

    @JsonValue
    public int toValue(){
        return this.ordinal();
    }
}
