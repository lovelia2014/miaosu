package com.miaosu.flux.orders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Lock repository
 * Created by angus on 15/10/7.
 */
@Repository
@Transactional(timeout = 10)
public class OrderDao extends JdbcDaoSupport {

    private static final String RECHARGEING_SQL = "update orders o set recharge_state = 1 where o.id = ? and recharge_state is null";

    private static final String RECHARGE_FAILED_SQL = "update orders o set o.recharge_state = 3, recharge_failed_reason = ?, refund_id = ?, recharge_end_time = now() where id = ? and refund_id is null";

    private static final String RECHARGE_SUCCESS_SQL = "update orders o set recharge_state = 2, recharge_end_time = now() where o.id = ?";

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        this.setDataSource(dataSource);
    }

    /**
     * 设置订单为充值中，只能将未充值的订单设置为充值中
     * @param orderId 订单编号
     * @return 更新行数
     */
    public int setToRecharging(final String orderId) {
        return getJdbcTemplate().update(RECHARGEING_SQL, orderId);
    }

    public int rechargeFailed(final String orderId, final String failedReason, final String refundId) {
        return getJdbcTemplate().update(RECHARGE_FAILED_SQL, failedReason, refundId, orderId);
    }

    public int rechargeSuccess(final String orderId) {
        return getJdbcTemplate().update(RECHARGE_SUCCESS_SQL, orderId);
    }
}
