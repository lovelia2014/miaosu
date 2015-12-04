package com.miaosu.flux.bills;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * Balance Repository
 * Created by angus on 15/10/2.
 */
@Repository
public interface BalanceRepository extends JpaRepository<Balance, String>{

}
