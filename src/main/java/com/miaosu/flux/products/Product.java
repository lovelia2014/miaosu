package com.miaosu.flux.products;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * 商品信息
 * Created by angus on 15/10/2.
 */
@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    private String id;

    private String name;

    /**
     * （QX:前向产品，HX:后向产品）
     */
    private String type;

    private BigDecimal price;

    private boolean enabled;

    @Column(name = "applicable_area")
    private String applicableArea;
}
