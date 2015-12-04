package com.miaosu.flux.orders;

import com.miaosu.flux.orders.enums.PayState;
import com.miaosu.flux.orders.enums.RechargeState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Product Repository
 * Created by angus on 15/10/2.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @Query("select o from Order o where create_time between ?4 and ?5"
            + " and (id = ?1 or ?1 is null or ?1 = '')"
            + " and (username = ?2 or ?2 is null or ?2 = '')"
            + " and (external_id = ?3 or ?3 is null or ?3 = '')"
            + " and (phone = ?6 or ?6 is null or ?6 = '')"
            + " and (o.effectType = ?7 or ?7 is null)"
            + " and (o.payState = ?8 or ?8 is null)"
            + " and (o.rechargeState = ?9 or ?9 is null)"
            + " and (o.productId = ?10 or ?10 is null or ?10 = '')"
            + " order by create_time desc")
    Page<Order> findByCondition(String id, String username, String externalId, Date begin, Date end, String phone,
                                Integer effectType, PayState payState, RechargeState rechargeState, String productId,
            Pageable pageable);

    @Query(value = "select * from orders o where create_time >= DATE_SUB(now(),INTERVAL 1 HOUR) and pay_state = 2 and (recharge_state = 0 or recharge_state is null) limit ?1", nativeQuery = true)
    List<Order> findUnRechargeOrders(int maxOrders);

    Order findByUsernameAndExternalId(String username, String externalId);

    Order findByIdAndUsername(String id, String username);

    Order findByCreateTimeAfterAndRechargeId(Date begin, String rechargeId);

    @Modifying
    @Query("update Order o set recharge_id = ?2, recharge_time = now(), recharge_system = ?3 where id = ?1")
    void setRechargeId(String id, String rechargeId, String rechargeSystem);

    @Modifying
    @Query("update Order o set o.payState = ?2, pay_failed_reason = ?3, pay_time = now() where o.id = ?1  ")
    void updatePayStatus(String orderId, PayState payState, String payFailedReason);

    @Query(value = "select o from Order o where recharge_time between ?1 and ?2 and recharge_state = 1")
    Page<Order> findUnknownRechargeStatusOrders(Date begin, Date end, Pageable pageable);

    @Query(value = "select * from orders o where create_time >= DATE_SUB(now(),INTERVAL 1 HOUR) and pay_state = 2 and recharge_state >= 1 and recharge_id is null limit ?1", nativeQuery = true)
    List<Order> findUnknownRechargeIdOrders(int maxOrders);
}
