package com.miaosu.flux.report.billstat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * 账单统计服务
 * Created by angus on 15/10/20.
 */
@Service
@Transactional(readOnly = true, timeout = 10)
public class BillStatService {
    @Autowired
    private BillStatDao billStatDao;

    @Autowired
    private BillStatRepository billStatRepository;

    public Page<BillStat> findByCondition(Date begin, Date end, String userName, Pageable pageable) {
        return billStatRepository.findByCondition(begin, end, userName, pageable);
    }

    public Map<String, BigDecimal> sumByStatDateBetween(Date begin, Date end, String userName) {
        return billStatDao.sumByStatDateBetween(begin, end, userName);
    }

    /**
     * 执行统计任务
     * @param beginDate 开始统计日期（包含），用yyyy-MM-dd格式
     * @param endDate 结束统计日期（不包含），用yyyy-MM-dd格式
     * @return 统计结果行数
     */
    @Transactional(timeout = 120)
    public int executeStat(String beginDate, String endDate) {
        return billStatRepository.executeStat(beginDate, endDate);
    }
}
