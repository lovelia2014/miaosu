package com.miaosu.flux.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 *
 * Created by angus on 15/10/7.
 */
public abstract class CustomRepository {
    private JdbcTemplate jdbcTemplate;

    protected int update(String sql, Objects args){
        Assert.notNull(jdbcTemplate, "jdbcTemplate not set");

        return jdbcTemplate.update(sql, args);
    }

    private void setJdbcTemplate(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
}
