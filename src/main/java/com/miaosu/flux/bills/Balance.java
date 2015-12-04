package com.miaosu.flux.bills;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
 * 余额
 * Created by angus on 15/10/4.
 */
@Data
@Entity
@Table(name = "balances")
@AllArgsConstructor
public class Balance {
    public Balance(){}

    public Balance(String username){
        this.username = username;
    }

    @Id
    private String username;

    @Column(insertable = false)
    private BigDecimal balance;

    @Column(name = "create_time", insertable = false, updatable = false)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private Date createTime;

    @Column(name = "last_change_time", insertable = false)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private Date lastChangeTime;
}
