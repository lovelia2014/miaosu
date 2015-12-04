package com.miaosu.flux.supplier.huazong.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 订购状态查询
 * Created by angus on 15/10/6.
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetOrderStatusResult extends Result {
    /**
     * 平台订单号
     */
    @JsonProperty("OrderNo")
    private String orderNo;

    /**
     * 订购状态（Y:订购成功,N:订购失败,P:订购中)
     */
    @JsonProperty("Status")
    private String status;

    /**
     * 失败原因
     */
    @JsonProperty("FailedReason")
    private String failedReason;
}
