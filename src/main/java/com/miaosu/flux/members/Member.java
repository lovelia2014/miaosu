package com.miaosu.flux.members;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miaosu.flux.util.CustomDateTimeDeserializer;
import com.miaosu.flux.util.CustomDateTimeSerializer;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 会员信息
 * Created by angus on 15/9/28.
 */
@Data
@Entity
@Table(name = "members")
public class Member implements Serializable {
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
    @Size(max = 64)
    @Column(name = "real_name")
    private String realName;

    /**
     * 身份证号码
     */
    @NotNull
    @Size(max = 32)
    @Column(name = "id_number")
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
    @Size(max = 32)
    @Column(name = "mobile_phone")
    private String mobilePhone;

    /**
     * 省份
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区县
     */
    private String area;

    /**
     * 详细地址
     */
    @Column(name = "detail_addr")
    private String detailAddr;

    /**
     * 注册时间
     */
    @Column(name = "reg_time", insertable = false)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private Date regTime;

    /**
     * 上次登录时间
     */
    @Column(name = "last_login_time", insertable = false)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private Date lastLoginTime;

    /**
     * 上次登录IP
     */
    @Column(name = "last_login_ip")
    private String lastLoginIp;

    /**
     * 会员折扣，不大于1
     */
    @Max(1)
    @Min(0)
    private BigDecimal discount;

    /**
     * 接口访问密钥，AES加密存储
     */
    private String token;
}
