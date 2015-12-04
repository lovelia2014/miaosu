package com.miaosu.flux.blacknums;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 黑名单号码信息
 * Created by angus on 15/10/2.
 */
@Data
@Entity
@Table(name = "blacknums")
public class BlackNum {
    @Id
    private String number;

    private String remark;
}
