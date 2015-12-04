package com.miaosu.flux.orders.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 充值状态（0：待充值；1：充值中；2：充值成功；3：充值失败；
 * Created by angus on 15/10/4.
 */
public enum RechargeState {
    INIT, PROCESS, SUCCESS, FAILED;

    @JsonCreator
    public static RechargeState forValue(int ordinal) {
        return RechargeState.values()[ordinal];
    }

    @JsonValue
    public int toValue(){
        return this.ordinal();
    }
}
