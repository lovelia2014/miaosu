package com.miaosu.flux.report.orderstat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.*;

/**
 * OrderStatDao
 * Created by angus on 15/10/20.
 */
@Repository
public class OrderStatDao extends JdbcDaoSupport {

    private Logger logger = LoggerFactory.getLogger(OrderStatDao.class);

    private static final String SUM_BY_STATDATE_BETWEEN = "select sum(total_count) as totalCount, sum(wait_recharge_sum) as waitRechargeSum, "
            + " sum(recharging_sum) as rechargingSum, sum(recharge_ok_sum) as rechargeOkSum, sum(recharge_fail_sum) as rechargeFailSum, "
            + " sum(total_price) as totalPrice, sum(wait_recharge_price_sum) as waitRechargePriceSum, sum(recharging_price_sum) as rechargingPriceSum, "
            + " sum(recharge_ok_price_sum) as rechargeOkPriceSum, sum(recharge_fail_price_sum) as rechargeFailPriceSum"
            + " from order_stats where stat_date between ? and ? "
            + " and (username = ? or ? is null or ? = '')"
            + " and (product_id = ? or ? is null or ? = '')"
            + " and (province like ? or ? is null or ? = '')";

    private static final String COUNT_SQL = "select count(1) from ";

    private static final String GROUP_COUNT_SQL_SELECT = "select stat_date "
            + "from order_stats o where o.stat_date between ? and ?"
            + "  and (o.username = ? or ? is null or ? = '')"
            + "  and (o.product_id = ? or ? is null or ? = '')"
            + "  and (o.province like ? or ? is null or ? = '')";

    private static final String GROUP_SQL_SELECT = "select stat_date, username, province, product_id, "
            + " sum(total_count) as total_count, "
            + " sum(wait_recharge_sum) as wait_recharge_sum, "
            + " sum(recharging_sum) as recharging_sum, "
            + " sum(recharge_ok_sum) as recharge_ok_sum, "
            + " sum(recharge_fail_sum) as recharge_fail_sum, "
            + " sum(total_price) as total_price, "
            + " sum(wait_recharge_price_sum) as wait_recharge_price_sum, "
            + " sum(recharging_price_sum) as recharging_price_sum, "
            + " sum(recharge_ok_price_sum) as recharge_ok_price_sum, "
            + " sum(recharge_fail_price_sum) as recharge_fail_price_sum "
            + "from order_stats o where o.stat_date between ? and ?"
            + "  and (o.username = ? or ? is null or ? = '')"
            + "  and (o.product_id = ? or ? is null or ? = '')"
            + "  and (o.province like ? or ? is null or ? = '')";

    private static final String GROUP_SQL_GROUP = " group by ";

    private static final String GROUP_SQL_LIMIT = " limit ?, ?";

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        this.setDataSource(dataSource);
    }

    public Page<OrderStat> findByGroup(Date begin, Date end, String userName, String productId, String province, final Boolean[] groupConditions, Pageable pageable) {

        String groupCondition = "";
        int idx = 0;
        for (boolean condition : groupConditions) {

            switch (idx) {
                case 0:
                    groupCondition = condition ? (StringUtils.hasText(groupCondition) ? groupCondition.concat(",stat_date") : groupCondition.concat("stat_date")) : groupCondition;
                    break;
                case 1:
                    groupCondition = condition ? (StringUtils.hasText(groupCondition) ? groupCondition.concat(",username") : groupCondition.concat("username")) : groupCondition;
                    break;
                case 2:
                    groupCondition = condition ? (StringUtils.hasText(groupCondition) ? groupCondition.concat(",province") : groupCondition.concat("province")) : groupCondition;
                    break;
                case 3:
                    groupCondition = condition ? (StringUtils.hasText(groupCondition) ? groupCondition.concat(",product_id") : groupCondition.concat("product_id")) : groupCondition;
                    break;
            }
            idx++;
        }

        String groupSql = GROUP_SQL_SELECT.concat(GROUP_SQL_GROUP).concat(groupCondition).concat(GROUP_SQL_LIMIT);
        String countSql = COUNT_SQL.concat(" (").concat(GROUP_COUNT_SQL_SELECT).concat(GROUP_SQL_GROUP).concat(groupCondition).concat(") t");

        logger.debug("groupSql: {}", groupSql);
        logger.debug("countSql: {}", countSql);

        long totalCount = this.getJdbcTemplate().queryForObject(countSql,
                new Object[]{begin, end, userName, userName, userName, productId, productId, productId, "%" + province + "%", province, province}, Long.class);

        int startRows = pageable.getOffset();
        int endRows = (pageable.getPageNumber() + 1) * pageable.getPageSize();

        List<OrderStat> resultList = this.getJdbcTemplate().query(groupSql,
                new Object[]{begin, end, userName, userName, userName, productId, productId, productId, "%" + province + "%", province, province, startRows, endRows},
                new RowMapper<OrderStat>() {
                    @Override
                    public OrderStat mapRow(ResultSet rs, int rowNum) throws SQLException {
                        OrderStat orderStat = new OrderStat();

                        int idx = 0;
                        for (boolean condition : groupConditions) {
                            switch (idx) {
                                case 0:
                                    orderStat.setStatDate(condition ? rs.getDate("stat_date") : null);
                                    break;
                                case 1:
                                    orderStat.setUsername(condition ? rs.getString("username") : "all");
                                    break;
                                case 2:
                                    orderStat.setProvince(condition ? rs.getString("province") : "all");
                                    break;
                                case 3:
                                    orderStat.setProductId(condition ? rs.getString("product_id") : "all");
                                    break;
                            }
                            idx++;
                        }

                        BigDecimal totalPrice = rs.getBigDecimal("total_price");
                        BigDecimal waitRechargePriceSum = rs.getBigDecimal("wait_recharge_price_sum");
                        BigDecimal rechargingPriceSum = rs.getBigDecimal("recharging_price_sum");
                        BigDecimal rechargeOkPriceSum = rs.getBigDecimal("recharge_ok_price_sum");
                        BigDecimal rechargeFailPriceSum = rs.getBigDecimal("recharge_fail_price_sum");
                        long totalCount = rs.getLong("total_count");
                        long rechargeOkSum = rs.getLong("recharge_ok_sum");
                        orderStat.setTotalCount(totalCount);
                        orderStat.setWaitRechargeSum(rs.getLong("wait_recharge_sum"));
                        orderStat.setRechargingSum(rs.getLong("recharging_sum"));
                        orderStat.setRechargeOkSum(rechargeOkSum);
                        orderStat.setRechargeFailSum(rs.getLong("recharge_fail_sum"));
                        orderStat.setTotalPrice(totalPrice == null ? BigDecimal.ZERO : totalPrice);
                        orderStat.setWaitRechargePriceSum(waitRechargePriceSum == null ? BigDecimal.ZERO : waitRechargePriceSum);
                        orderStat.setRechargingPriceSum(rechargingPriceSum == null ? BigDecimal.ZERO : rechargingPriceSum);
                        orderStat.setRechargeOkPriceSum(rechargeOkPriceSum == null ? BigDecimal.ZERO : rechargeOkPriceSum);
                        orderStat.setRechargeFailPriceSum(rechargeFailPriceSum == null ? BigDecimal.ZERO : rechargeFailPriceSum);
                        return orderStat;
                    }
                });

        resultList = totalCount > pageable.getOffset() ? resultList : Collections.<OrderStat>emptyList();

        return new PageImpl<>(resultList, pageable, totalCount);
    }

    /**
     * 根据条件汇总数据
     *
     * @param begin    开始日期
     * @param end      结束日期
     * @param userName 用户名
     * @return 数据
     */
    public Map<String, Object> sumByStatDateBetween(Date begin, Date end, String userName, String productId, String province) {
        return this.getJdbcTemplate().queryForObject(SUM_BY_STATDATE_BETWEEN,
                new Object[]{begin, end, userName, userName, userName, productId, productId, productId, "%" + province + "%", province, province},
                new RowMapper<Map<String, Object>>() {
                    @Override
                    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Map<String, Object> result = new HashMap<>();
                        BigDecimal totalPrice = rs.getBigDecimal("totalPrice");
                        BigDecimal waitRechargePriceSum = rs.getBigDecimal("waitRechargePriceSum");
                        BigDecimal rechargingPriceSum = rs.getBigDecimal("rechargingPriceSum");
                        BigDecimal rechargeOkPriceSum = rs.getBigDecimal("rechargeOkPriceSum");
                        BigDecimal rechargeFailPriceSum = rs.getBigDecimal("rechargeFailPriceSum");
                        long totalCount = rs.getLong("totalCount");
                        long rechargeOkSum = rs.getLong("rechargeOkSum");

                        NumberFormat numberFormat = NumberFormat.getPercentInstance();
                        numberFormat.setMaximumFractionDigits(2);
                        numberFormat.setMinimumFractionDigits(2);
                        String rechargeOkRate = "N/A";
                        if (totalCount != 0l) {
                            rechargeOkRate = numberFormat.format(Double.longBitsToDouble(rechargeOkSum) / Double.longBitsToDouble(totalCount));
                        }

                        result.put("a_totalCount", totalCount);
                        result.put("b_waitRechargeSum", rs.getLong("waitRechargeSum"));
                        result.put("c_rechargingSum", rs.getLong("rechargingSum"));
                        result.put("d_rechargeOkSum", rechargeOkSum);
                        result.put("e_rechargeFailSum", rs.getLong("rechargeFailSum"));
                        result.put("f_totalPrice", totalPrice == null ? BigDecimal.ZERO : totalPrice);
                        result.put("g_waitRechargePriceSum", waitRechargePriceSum == null ? BigDecimal.ZERO : waitRechargePriceSum);
                        result.put("h_rechargingPriceSum", rechargingPriceSum == null ? BigDecimal.ZERO : rechargingPriceSum);
                        result.put("i_rechargeOkPriceSum", rechargeOkPriceSum == null ? BigDecimal.ZERO : rechargeOkPriceSum);
                        result.put("j_rechargeFailPriceSum", rechargeFailPriceSum == null ? BigDecimal.ZERO : rechargeFailPriceSum);
                        result.put("k_rechargeOkRate", rechargeOkRate);
                        return result;
                    }
                });
    }
}
