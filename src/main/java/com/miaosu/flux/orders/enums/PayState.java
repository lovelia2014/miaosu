package com.miaosu.flux.orders.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 支付状态（0：待支付；1：支付中；2：已支付；3：支付失败；4：退款中；5：已退款）
 * Created by angus on 15/10/4.
 */
public enum PayState {
    INIT, PROCESS, SUCCESS, FAILED, REFUND_PROCESS, REFUNDED;

    @JsonCreator
    public static PayState forValue(int ordinal) {
        return PayState.values()[ordinal];
    }

    @JsonValue
    public int toValue(){
        return this.ordinal();
    }
}
