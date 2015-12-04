package com.miaosu.flux.supplier.huazong.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 订购接口返回对象
 * Created by angus on 15/10/6.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderFlowResult extends Result {
    /**
     * 状态（Y:订购已受理,N:失败)
     */
    @JsonProperty("Status")
    private String status;

    /**
     * 失败原因(成功时为空值)
     */
    @JsonProperty("FailedReason")
    private String failedReason;

    /**
     * 订单编号
     */
    @JsonProperty("OrderNo")
    private String orderNo;
}
