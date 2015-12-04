package com.miaosu.flux.bills.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 流水类型(0：加钱；1：减钱；)
 * Created by angus on 15/10/4.
 */
public enum BillType {
    ADD, SUBTRACTION;

    @JsonCreator
    public static BillType forValue(int ordinal) {
        return BillType.values()[ordinal];
    }

    @JsonValue
    public int toValue(){
        return this.ordinal();
    }
}
