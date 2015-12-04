package com.miaosu.flux.members;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miaosu.flux.util.CustomDateTimeDeserializer;
import com.miaosu.flux.util.CustomDateTimeSerializer;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 会员Form
 * Created by angus on 15/9/29.
 */
@Data
public class MemberForm implements Serializable {
    /**
     * 会员名
     */
    @Id
    @NotNull
    @Size(min = 6, max = 64)
    private String username;

    /**
     * 真实名称
     */
    @NotNull
    @Size(min = 6, max = 64)
    private String realName;

    /**
     * 身份证号码
     */
    @NotNull
    @Size(min = 6, max = 32)
    private String idNumber;

    /**
     * 性别；0：女；1：男
     */
    @Min(0)
    @Max(1)
    private int sex;

    /**
     * 手机号码
     */
    @NotNull
    @Size(min = 6, max = 32)
    private String mobilePhone;

    /**
     * 地址
     */
    private String address;

    /**
     * 注册时间
     */
    @Column(insertable = false)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private Date regTime;

    /**
     * 上次登录时间
     */
    @Column(insertable = false)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private Date lastLoginTime;

    /**
     * 上次登录IP
     */
    private String lastLoginIp;

    /**
     * 会员折扣，不大于1
     */
    @Max(1)
    @Min(0)
    private BigDecimal discount;

    /**
     * 用户状态
     */
    private boolean enabled;

    /**
     * 用户余额
     */
    private BigDecimal balance;
}
