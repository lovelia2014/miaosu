package com.miaosu.flux.system.menus;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * 菜单数据层
 * Created by angus on 15/6/15.
 */
@Repository
public interface MenuRepository extends PagingAndSortingRepository<Menu, Long> {

}
