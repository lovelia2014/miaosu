package com.miaosu.flux.locks;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.net.InetAddress;

/**
 * Lock repository
 * Created by angus on 15/10/7.
 */
@Repository
@Transactional(timeout=10)
public class LockDao extends JdbcDaoSupport {

    private static final String ACQUIRE_LOCK_SQL = "update locks set locked=true, locked_time=now(), locked_by=? where name=?";

    private static final String RELEASE_LOCK_SQL = "update locks set locked=false, locked_time=null, locked_by=null where name=?";

    private static final String QUERY_LOCK_SQL = "select locked from locks where name=?";

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init(){
        this.setDataSource(dataSource);
    }

    public boolean acquireLock(String name) {
        Boolean locked = getJdbcTemplate().queryForObject(QUERY_LOCK_SQL, Boolean.class, name);

        if(locked){
            return false;
        }else {
            String lockedBy = null;
            try {
                lockedBy = InetAddress.getLocalHost().getHostAddress();
            }catch (Exception ex) {
                //ignore
            }

            int rows = getJdbcTemplate().update(ACQUIRE_LOCK_SQL, lockedBy, name);
            if (rows == 1){
                return true;
            }
        }
        return false;
    }

    public void releaseLock(String name){
        int rows = getJdbcTemplate().update(RELEASE_LOCK_SQL, name);

        if(rows != 1) {
            throw new RuntimeException("没有正确的释放锁。name: " + name);
        }
    }
}
