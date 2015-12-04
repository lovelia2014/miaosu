package com.miaosu.flux.bills;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Balance service
 * Created by angus on 15/10/4.
 */
@Service
@Transactional(readOnly = true, timeout = 10)
public class BalanceService {

    @Autowired
    private BalanceRepository balanceRepository;

    public Balance get(String userName) {
        return balanceRepository.findOne(userName);
    }
}
