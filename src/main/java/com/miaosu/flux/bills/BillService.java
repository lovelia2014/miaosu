package com.miaosu.flux.bills;

import com.miaosu.flux.bills.enums.BillChannel;
import com.miaosu.flux.bills.enums.BillStatus;
import com.miaosu.flux.bills.enums.BillType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * 账单Service
 * Created by angus on 15/10/2.
 */
@Service
@Transactional(readOnly = true, timeout = 10)
public class BillService {

    @Autowired
    private BillRepository billRepository;

    public Page<Bill> findByCondition(Date begin, Date end, String username, BillType type, BillChannel channel, BillStatus status, Pageable pageable) {
        if(begin == null) {
            // begin为空时默认查询一天内的账单
            begin = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        }
        if(end == null) {
            end = new Date();
        }

        return billRepository.findByCondition(begin, end, username, type, channel, status, pageable);
    }

    @Deprecated
    public Page<Bill> find(String username, Date begin, Date end, Pageable pageable) {
        if(begin == null) {
            begin = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        }
        if(end == null) {
            end = new Date();
        }

        if (StringUtils.hasText(username)) {
            return billRepository.findByUsernameAndCreateTimeBetweenOrderByCreateTimeDesc(username, begin, end,
                    pageable);
        } else {
            return billRepository.findByCreateTimeBetweenOrderByCreateTimeDesc(begin, end, pageable);
        }
    }



//    @Modifying
//    @Transactional
//    public Bill create(Bill bill) {
//        String id = bill.getId();
//        if (billRepository.exists(id)) {
//            throw new ServiceException(ResultCode.DATA_EXISTS);
//        }
//
//        return billRepository.saveAndFlush(bill);
//    }

}
