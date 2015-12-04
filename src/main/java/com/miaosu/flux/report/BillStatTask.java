package com.miaosu.flux.report;

import com.miaosu.flux.locks.LockService;
import com.miaosu.flux.report.billstat.BillStatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 账单统计任务
 * Created by angus on 15/10/20.
 */
@Component
public class BillStatTask  implements DisposableBean {
    private static final String BILL_STAT_LOCK = "bill_stats_lock";

    private static final Logger logger = LoggerFactory.getLogger(BillStatTask.class);

    @Autowired
    private LockService lockService;

    @Autowired
    private BillStatService billStatService;

    /**
     * 每天凌晨2点10分执行
     */
    @Scheduled(cron = "0 10 3 * * *")
    public void execute() {

        // 获取锁
        boolean locked = lockService.acquireLock(BILL_STAT_LOCK);

        if (locked) {
            try {
                logger.debug("获取到账单统计锁");

                Date now = new Date();
                Date yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                int rows = billStatService.executeStat(simpleDateFormat.format(yesterday), simpleDateFormat.format(now));

                logger.info("账单统计任务完成，生成数据{}行", rows);
            } catch (Exception ex) {
                logger.warn("账单统计任务发生异常", ex);
            } finally {
                lockService.releaseLock(BILL_STAT_LOCK);
            }

        } else {
            logger.debug("未获取到账单统计锁！");
        }
    }

    @Override
    public void destroy() throws Exception {
        lockService.releaseLock(BILL_STAT_LOCK);
    }
}
