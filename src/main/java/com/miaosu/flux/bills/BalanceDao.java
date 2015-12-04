package com.miaosu.flux.bills;

import java.math.BigDecimal;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lock repository
 * Created by angus on 15/10/7.
 */
@Repository
@Transactional(timeout = 10)
public class BalanceDao extends JdbcDaoSupport {

    private static final String SUBTRACT_SQL = "update balances b set b.balance = b.balance - ?, last_change_time = now() where b.username = ? and b.balance >= ? ";

    private static final String ADD_SQL = "update balances b set b.balance = b.balance + ?, last_change_time = now() where b.username = ?";

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        this.setDataSource(dataSource);
    }

    public int subtract(final String userName, final BigDecimal amount) {
        return getJdbcTemplate().update(SUBTRACT_SQL, amount, userName, amount);
    }

    public int add(final String userName, final BigDecimal amount) {
        return getJdbcTemplate().update(ADD_SQL, amount, userName);
    }

}
