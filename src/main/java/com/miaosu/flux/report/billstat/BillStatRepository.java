package com.miaosu.flux.report.billstat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * 账单统计Repository
 * Created by angus on 15/10/20.
 */
@Repository
public interface BillStatRepository extends JpaRepository<BillStat, Long>{

    @Query("select b from BillStat b where b.statDate between ?1 and ?2 and (b.username = ?3 or ?3 is null or ?3 = '')")
    Page<BillStat> findByCondition(Date begin, Date end, String userName, Pageable pageable);

    @Query(value = "select stat_bills(?1, ?2)", nativeQuery = true)
    int executeStat(String beginDate, String endDate);
}
