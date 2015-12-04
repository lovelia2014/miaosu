package com.miaosu.flux.system.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 用户信息
 * Created by angus on 15/6/19.
 */
@Entity
@Table(name = "users")
@Data
public class User implements Serializable {
    /**
     * 会员名
     */
    @Id
    private String username;

    /**
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * 启用状态；0：禁用；1：启用；
     */
    private boolean enabled;
}
