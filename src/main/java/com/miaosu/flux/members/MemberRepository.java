package com.miaosu.flux.members;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Member Repository
 * Created by angus on 15/6/19.
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
    Page<Member> findByUsernameLikeAndRegTimeBetween(String userName, Date beginRegTime, Date endRegTime, Pageable pageable);

    @Query(value = "select u from Member u where u.username like %?1% or real_name like %?1% or mobile_phone like %?1%")
    Page<Member> findByCondition(String text, Pageable pageable);

    @Modifying
    @Query("update Member u set u.token = ?2 where u.username = ?1 ")
    void resetToken(String userName, String token);

    @Modifying
    @Query("update Member u set u.lastLoginTime = ?2, u.lastLoginIp = ?3 where u.username = ?1")
    void updateLastLoginInfo(String userName, Date lastLoginTime, String lastLoginIp);

}
