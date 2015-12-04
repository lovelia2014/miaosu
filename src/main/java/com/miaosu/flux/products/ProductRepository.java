package com.miaosu.flux.products;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Product Repository
 * Created by angus on 15/10/2.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, String>{

    @Query("select p from Product p where p.id like %?1% or p.name like %?1%")
    Page<Product> findByCondition(String text, Pageable pageable);

    @Modifying
    @Query("update Product p set enabled = 0 where id = ?1")
    void disable(String id);

    @Modifying
    @Query("update Product p set enabled = 1 where id = ?1")
    void enable(String id);
}
