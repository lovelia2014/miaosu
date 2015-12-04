package com.miaosu.flux.report.billstat;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miaosu.flux.util.CustomDateDeserializer;
import com.miaosu.flux.util.CustomDateSerializer;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 账单统计对象
 * Created by angus on 15/10/20.
 */
@Data
@Entity
@Table(name = "bill_stats")
public class BillStat {
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
     * 充值金额
     */
    @Column(name = "add_sum")
    private BigDecimal addSum;

    /**
     * 支付金额
     */
    @Column(name = "pay_sum")
    private BigDecimal paySum;

    /**
     * 退款金额
     */
    @Column(name = "refund_sum")
    private BigDecimal refundSum;

    /**
     * 奖励金额
     */
    @Column(name = "reward_sum")
    private BigDecimal rewardSum;

    /**
     * 其他金额
     */
    @Column(name = "others_sum")
    private BigDecimal othersSum;
}
