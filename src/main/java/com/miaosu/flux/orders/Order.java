package com.miaosu.flux.orders;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miaosu.flux.orders.enums.PayState;
import com.miaosu.flux.orders.enums.RechargeState;
import com.miaosu.flux.util.CustomDateTimeDeserializer;
import com.miaosu.flux.util.CustomDateTimeSerializer;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单信息
 * Created by angus on 15/10/2.
 */
@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    private String id;

    @NotNull
    private String username;

    @NotNull
    private String phone;

    /**
     * 到账类型（0：立即到账；1：下月生效）
     */
    @NotNull
    @Column(name = "effect_type")
    private Integer effectType;

    private  String province;

    @NotNull
    @Column(name = "external_id")
    private String externalId;

    @NotNull
    @Column(name = "product_id")
    private String productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_price")
    private BigDecimal productPrice;

    private BigDecimal price;

    /**
     * 支付状态（0：待支付；1：支付中；2：已支付；3：支付失败；4：退款中；5：已退款）
     */
    @Column(name = "pay_state")
    private PayState payState;

    @Column(name = "pay_failed_reason")
    private String payFailedReason;

    @Column(name = "pay_id")
    private String payId;

    @Column(name = "refund_id")
    private String refundId;

    @Column(name = "recharge_id")
    private String rechargeId;

    @Column(name = "recharge_system")
    private String rechargeSystem;

    /**
     * 充值状态（0：待充值；1：充值中；2：充值成功；3：充值失败；
     */
    @Column(name = "recharge_state")
    private RechargeState rechargeState;

    @Column(name = "recharge_failed_reason")
    private String rechargeFailedReason;

    @Column(name = "notify_url")
    private String notifyUrl;

    @Column(name = "pay_time")
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private Date payTime;

    @Column(name = "recharge_time")
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private Date rechargeTime;

    @Column(name = "create_time", insertable = false, updatable = false)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private Date createTime;

    @Column(name = "recharge_end_time")
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private Date rechargeEndTime;
}
