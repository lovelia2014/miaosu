package com.miaosu.flux.blacknums;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miaosu.flux.base.ResultCode;
import com.miaosu.flux.base.ServiceException;


/**
 * 黑名单号码Service
 * Created by angus on 15/10/2.
 */
@Service
@Transactional(readOnly = true, timeout = 10)
public class BlackNumService {

    @Autowired
    private BlackNumRepository blackNumRepository;

    public Page<BlackNum> find(String text, Pageable pageable) {
        if (text == null) {
            text = "";
        }
        return blackNumRepository.findByNumberLike(text, pageable);
    }

    public BlackNum get(String id) {
        return blackNumRepository.findOne(id);
    }


    @Modifying
    @Transactional
    public void remove(String... ids) {
        for (String id : ids) {
            blackNumRepository.delete(id);
        }
    }

    @Modifying
    @Transactional
    public BlackNum create(BlackNum blackNum) {
        String id = blackNum.getNumber();
        if (blackNumRepository.exists(id)) {
            throw new ServiceException(ResultCode.DATA_EXISTS);
        }

        return blackNumRepository.saveAndFlush(blackNum);
    }

    @Modifying
    @Transactional
    public BlackNum update(BlackNum blackNum) {
        String id = blackNum.getNumber();
        if (!blackNumRepository.exists(id)) {
            throw new ServiceException(ResultCode.DATA_NOT_EXISTS);
        }

        return blackNumRepository.saveAndFlush(blackNum);
    }

}
