package com.miaosu.flux.system.menus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Menu业务层实现
 * Created by angus on 15/6/15.
 */
@Service
@Transactional(readOnly = true, timeout = 10)
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    public Iterable<Menu> findAll() {
        return menuRepository.findAll();
    }

    @Modifying
    @Transactional
    public Menu save(Menu menu) {
        return menuRepository.save(menu);
    }

    @Modifying
    @Transactional
    public void remove(Long... ids) {
        for(Long id : ids) {
            menuRepository.delete(id);
        }
    }
}
