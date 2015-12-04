package com.miaosu.flux.supplier.huazong.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 返回结果基类
 * Created by angus on 15/10/6.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Result implements Serializable{
    @JsonProperty("Code")
    private String code;
}
