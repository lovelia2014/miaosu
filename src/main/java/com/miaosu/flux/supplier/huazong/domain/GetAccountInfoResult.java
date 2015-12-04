package com.miaosu.flux.supplier.huazong.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 余额查询
 * Created by angus on 15/10/6.
 */
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetAccountInfoResult extends Result {
    /**
     * 账户余额
     */
    @JsonProperty("AccountBalance")
    private String AccountBalance;
}
