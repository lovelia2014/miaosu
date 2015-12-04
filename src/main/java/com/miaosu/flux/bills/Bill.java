package com.miaosu.flux.bills;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miaosu.flux.bills.enums.BillChannel;
import com.miaosu.flux.bills.enums.BillStatus;
import com.miaosu.flux.bills.enums.BillType;
import com.miaosu.flux.util.CustomDateTimeDeserializer;
import com.miaosu.flux.util.CustomDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 账单
 * Created by angus on 15/10/2.
 */
@Data
@Entity
@Table(name = "bills")
@AllArgsConstructor
public class Bill {
    public Bill(){

    }

    @Id
    private String id;

    private String username;

    private BigDecimal amt;

    @Column(name = "old_balance")
    private BigDecimal oldBalance;

    /**
     * 流水类型(0：加钱；1：减钱；)
     */
    private BillType type;

    /**
     * 流水来源（0：充值；1：支付；2：退款；3：奖励；9：其他；）
     */
    private BillChannel channel;

    private String info;

    /**
     * 账单处理状态（0: 未处理；1：处理中；2：处理成功；3：处理失败）
     */
    private BillStatus status;

    @Column(name = "create_time")
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private Date createTime;
}
