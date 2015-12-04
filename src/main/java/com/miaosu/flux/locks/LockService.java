package com.miaosu.flux.locks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Lock Service
 * Created by angus on 15/10/7.
 */
@Service
public class LockService {

    private static Logger logger = LoggerFactory.getLogger(LockService.class);

    @Autowired
    private LockDao lockDao;

    public boolean acquireLock(String name) {
        try {
            return lockDao.acquireLock(name);
        }catch(Exception ex){
            logger.warn("获取{}锁失败", name, ex);
        }
        return false;
    }

    public void releaseLock(String name) {
        try{
            lockDao.releaseLock(name);
        }catch(Exception ex){
            logger.warn("释放{}锁失败", name, ex);
        }
    }
}
