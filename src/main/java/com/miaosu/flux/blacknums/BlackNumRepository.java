package com.miaosu.flux.blacknums;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Product Repository
 * Created by angus on 15/10/2.
 */
@Repository
public interface BlackNumRepository extends JpaRepository<BlackNum, String>{
    @Query("select p from BlackNum p where p.number like %?1%")
    Page<BlackNum> findByNumberLike(String number, Pageable pageable);
}
