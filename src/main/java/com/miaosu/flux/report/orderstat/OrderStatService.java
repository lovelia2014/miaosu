package com.miaosu.flux.report.orderstat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

/**
 * 订单统计服务
 * Created by angus on 15/10/20.
 */
@Service
@Transactional(readOnly = true, timeout = 10)
public class OrderStatService {
    @Autowired
    private OrderStatDao orderStatDao;

    @Autowired
    private OrderStatRepository orderStatRepository;

    public Page<OrderStat> findByCondition(Date begin, Date end, String userName, String productId, String province, Pageable pageable) {
        return orderStatRepository.findByCondition(begin, end, userName, productId, province, pageable);
    }

    public Map<String, Object> sumByStatDateBetween(Date begin, Date end, String userName, String productId, String province) {
        return orderStatDao.sumByStatDateBetween(begin, end, userName, productId, province);
    }

    /**
     * 执行统计任务
     * @param beginDate 开始统计日期（包含），用yyyy-MM-dd格式
     * @param endDate 结束统计日期（不包含），用yyyy-MM-dd格式
     * @return 统计结果行数
     */
    @Transactional(timeout = 120)
    public int executeStat(String beginDate, String endDate) {
        return orderStatRepository.executeStat(beginDate, endDate);
    }


    /**
     * 按分组查询统计数据
     */
    public Page<OrderStat> findByGroup(Date begin, Date end, String userName, String productId, String province, final Boolean[] groupConditions, Pageable pageable) {
        return orderStatDao.findByGroup(begin, end, userName, productId, province, groupConditions, pageable);
    }
}
