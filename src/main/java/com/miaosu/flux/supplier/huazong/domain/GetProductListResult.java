package com.miaosu.flux.supplier.huazong.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.ToString;

import java.util.List;

/**
 * 商品列表查询
 * Created by angus on 15/10/6.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetProductListResult extends Result {
    /**
     * 账户余额
     */
    @JsonProperty("Products")
    private List<Product> products;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Product {
        @JsonProperty("ProductCode")
        private String productCode;

        @JsonProperty("ProductName")
        private String productName;

        @JsonProperty("ProductType")
        private String productType;

        @JsonProperty("ProductPrice")
        private String productPrice;

        @JsonProperty("ApplicableArea")
        private String applicableArea;
    }
}
