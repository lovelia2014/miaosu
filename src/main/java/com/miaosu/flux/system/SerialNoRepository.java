package com.miaosu.flux.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Serial Repository
 * Created by angus on 15/9/29.
 */
public interface SerialNoRepository extends JpaRepository<SerialNo, Long>{

    @Query(value = "select cur_val(?1)", nativeQuery = true)
    Long curVal(String seqName);

    @Query(value = "select next_val(?1)", nativeQuery = true)
    Long nextVal(String seqName);
}
