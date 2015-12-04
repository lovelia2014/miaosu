package com.miaosu.flux.bills.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 账单处理状态（0: 未处理；1：处理中；2：处理成功；3：处理失败）
 * Created by angus on 15/10/4.
 */
public enum BillStatus {
    INIT, PROCESS, SUCCESS, FAILED;

    @JsonCreator
    public static BillStatus forValue(int ordinal) {
        return BillStatus.values()[ordinal];
    }

    @JsonValue
    public int toValue(){
        return this.ordinal();
    }
}
