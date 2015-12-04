package com.miaosu.flux.bills;

import com.miaosu.flux.bills.enums.BillChannel;
import com.miaosu.flux.bills.enums.BillStatus;
import com.miaosu.flux.bills.enums.BillType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

/**
 * Bill Repository
 * Created by angus on 15/10/2.
 */
public interface BillRepository extends JpaRepository<Bill, String> {

    @Query("select b from Bill b where create_time between ?1 and ?2 "
            + " and (username = ?3 or ?3 is null or ?3 = '') "
            + " and (b.type = ?4 or ?4 is null) "
            + " and (b.channel = ?5 or ?5 is null) "
            + " and (b.status = ?6 or ?6 is null) "
            + "order by create_time desc")
    Page<Bill> findByCondition(Date begin, Date end, String username, BillType type, BillChannel channel, BillStatus status, Pageable pageable);

    Page<Bill> findByUsernameAndCreateTimeBetweenOrderByCreateTimeDesc(String username, Date begin, Date end, Pageable pageable);

    Page<Bill> findByCreateTimeBetweenOrderByCreateTimeDesc(Date begin, Date end, Pageable pageable);

    @Modifying
    @Query("update Bill b set b.status = ?2 where b.id = ?1")
    void updateBillStatus(String billId, BillStatus billStatus);
}
