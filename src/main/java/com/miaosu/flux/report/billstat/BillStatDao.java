package com.miaosu.flux.report.billstat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * BillStatDao
 * Created by angus on 15/10/20.
 */
@Repository
public class BillStatDao extends JdbcDaoSupport {

    private static final String SUM_BY_STATDATE_BETWEEN = "select sum(add_sum) as totalAddSum, sum(pay_sum) as totalPaySum, sum(refund_sum) as totalRefundSum, "
            + "sum(reward_sum) as totalRewardSum, sum(others_sum) as totalOthersSum "
            + "from bill_stats where stat_date between ? and ? and (username = ? or ? is null or ? = '')";


    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        this.setDataSource(dataSource);
    }

    /**
     * 根据条件汇总数据
     *
     * @param begin    开始日期
     * @param end      结束日期
     * @param userName 用户名
     * @return 数据
     */
    public Map<String, BigDecimal> sumByStatDateBetween(Date begin, Date end, String userName) {
        return this.getJdbcTemplate().queryForObject(SUM_BY_STATDATE_BETWEEN, new Object[]{begin, end, userName, userName, userName},
                new RowMapper<Map<String, BigDecimal>>() {
                    @Override
                    public Map<String, BigDecimal> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Map<String, BigDecimal> result = new HashMap<>();
                        BigDecimal totalAddSum = rs.getBigDecimal("totalAddSum");
                        BigDecimal totalPaySum = rs.getBigDecimal("totalPaySum");
                        BigDecimal totalRefundSum = rs.getBigDecimal("totalRefundSum");
                        BigDecimal totalRewardSum = rs.getBigDecimal("totalRewardSum");
                        BigDecimal totalOthersSum = rs.getBigDecimal("totalOthersSum");
                        result.put("a_totalAddSum", totalAddSum == null ? BigDecimal.ZERO : totalAddSum);
                        result.put("b_totalPaySum", totalPaySum == null ? BigDecimal.ZERO : totalPaySum);
                        result.put("c_totalRefundSum", totalRefundSum == null ? BigDecimal.ZERO : totalRefundSum);
                        result.put("d_totalRewardSum", totalRewardSum == null ? BigDecimal.ZERO : totalRewardSum);
                        result.put("e_totalOthersSum", totalOthersSum == null ? BigDecimal.ZERO : totalOthersSum);
                        return result;
                    }
                });
    }
}
