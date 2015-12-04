package com.miaosu.flux.supplier.huazong.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 校验订购接口返回对象
 * Created by angus on 15/10/6.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckOrderFlowResult extends Result{

    /**
     * 状态（Y:可以订购,N:不可订购)
     */
    @JsonProperty("Status")
    private String status;

    /**
     * 不可订购的原因(成功时为空值)
     */
    @JsonProperty("FailedReason")
    private String failedReason;

    /**
     * 平台订单号
     */
    @JsonProperty("OrderNo")
    private String orderNo;
}
