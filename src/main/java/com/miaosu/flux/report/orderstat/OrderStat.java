package com.miaosu.flux.report.orderstat;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miaosu.flux.util.CustomDateDeserializer;
import com.miaosu.flux.util.CustomDateSerializer;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单统计对象
 * Created by angus on 15/10/20.
 */
@Data
@Entity
@Table(name = "order_stats")
public class OrderStat {
    @Id
    @GeneratedValue
    private Long id;

    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Column(name = "stat_date")
    private Date statDate;

    /**
     * 用户名
     */
    private String username;

    /**
     * 商品代码
     */
    @Column(name = "product_id")
    private String productId;

    /**
     * 省份
     */
    private String province;

    /**
     * 订购总单数(只算付款成功后的）
     */
    @Column(name = "total_count")
    private Long totalCount;

    /**
     * 待充值订单数
     */
    @Column(name = "wait_recharge_sum")
    private Long waitRechargeSum;

    /**
     * 充值中订单数
     */
    @Column(name = "recharging_sum")
    private Long rechargingSum;

    /**
     * 充值成功订单数
     */
    @Column(name = "recharge_ok_sum")
    private Long rechargeOkSum;

    /**
     * 充值失败订单数
     */
    @Column(name = "recharge_fail_sum")
    private Long rechargeFailSum;

    /**
     * 订购总金额(只算付款成功后的）
     */
    @Column(name = "total_price")
    private BigDecimal totalPrice;

    /**
     * 待充值总金额
     */
    @Column(name = "wait_recharge_price_sum")
    private BigDecimal waitRechargePriceSum;

    /**
     * 充值中总金额
     */
    @Column(name = "recharging_price_sum")
    private BigDecimal rechargingPriceSum;

    /**
     * 充值成功总金额
     */
    @Column(name = "recharge_ok_price_sum")
    private BigDecimal rechargeOkPriceSum;

    /**
     * 充值失败总金额
     */
    @Column(name = "recharge_fail_price_sum")
    private BigDecimal rechargeFailPriceSum;

}
