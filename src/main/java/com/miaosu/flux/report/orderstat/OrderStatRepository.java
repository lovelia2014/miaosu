package com.miaosu.flux.report.orderstat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * 订单统计Repository
 * Created by angus on 15/10/20.
 */
@Repository
public interface OrderStatRepository extends JpaRepository<OrderStat, Long> {

    @Query("select o from OrderStat o where o.statDate between ?1 and ?2"
            + " and (o.username = ?3 or ?3 is null or ?3 = '')"
            + " and (o.productId = ?4 or ?4 is null or ?4 = '')"
            + " and (o.province like %?5% or ?5 is null or ?5 = '')")
    Page<OrderStat> findByCondition(Date begin, Date end, String userName, String productId, String province, Pageable pageable);

    @Query(value = "select stat_orders(?1, ?2)", nativeQuery = true)
    int executeStat(String beginDate, String endDate);
}
