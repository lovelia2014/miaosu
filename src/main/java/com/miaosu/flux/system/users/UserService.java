package com.miaosu.flux.system.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User Service
 * Created by angus on 15/6/19.
 */
@Service
@Transactional(readOnly = true, timeout = 10)
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDetailsManager userDetailsManager;

    @SuppressWarnings("deprecation")
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Cache userCache;

    /**
     * 修改用户密码
     *
     * @param userName    用户名
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @Modifying
    @Transactional
    public boolean changePassword(String userName, String oldPassword, String newPassword) {
        int rows = userRepository.changePassword(userName, passwordEncoder.encodePassword(oldPassword, null), passwordEncoder.encodePassword(newPassword, null));
//        userDetailsManager.changePassword(oldPassword, passwordEncoder.encodePassword(newPassword, null));
        try {
            userCache.evict(userName);
        } catch (Exception ex) {
            logger.info("Remove user from cache failed. userName:{}, exMsg:{}", userName, ex.getMessage());
        }
        return rows == 1;
    }

    /**
     * 判断用户是否存在
     *
     * @param userName 用户名
     * @return 是否存在
     */
    public boolean userExists(String userName) {
        return userDetailsManager.userExists(userName);
    }

    /**
     * 创建用户
     *
     * @param userName    用户名
     * @param password    密码
     * @param enable      是否启用
     * @param authorities 权限信息
     */
    public void create(String userName, String password, boolean enable, List<GrantedAuthority> authorities) {
        userDetailsManager.createUser(new User(userName, passwordEncoder.encodePassword(password, null), enable, false, false, false, authorities));
    }

    /**
     * 修改用户
     *
     * @param userName    用户名
     * @param password    密码
     * @param enable      是否启用
     * @param authorities 权限信息
     */
    public void update(String userName, String password, boolean enable, List<GrantedAuthority> authorities) {
        try {
            userCache.evict(userName);
        } catch (Exception ex) {
            logger.info("Remove user from cache failed. userName:{}, exMsg:{}", userName, ex.getMessage());
        }
        userDetailsManager.updateUser(new User(userName, passwordEncoder.encodePassword(password, null), enable, false, false, false, authorities));
    }

    /**
     * 删除用户
     *
     * @param userNames 用户名数组
     */
    @Modifying
    @Transactional
    public void delete(String... userNames) {
        for (String userName : userNames) {
            try {
                userCache.evict(userName);
            } catch (Exception ex) {
                logger.info("Remove user from cache failed. userName:{}, exMsg:{}", userName, ex.getMessage());
            }
            userDetailsManager.deleteUser(userName);
        }
    }

    /**
     * 禁用用户
     *
     * @param userNames 用户名数组
     */
    @Modifying
    @Transactional
    public void disable(String... userNames) {
        for (String userName : userNames) {
            try {
                userCache.evict(userName);
            } catch (Exception ex) {
                logger.info("Remove user from cache failed. userName:{}, exMsg:{}", userName, ex.getMessage());
            }
            userRepository.setEnabled(userName, false);
        }
    }

    /**
     * 启用用户
     *
     * @param userNames 用户名数组
     */
    @Modifying
    @Transactional
    public void enable(String... userNames) {
        for (String userName : userNames) {
            try {
                userCache.evict(userName);
            } catch (Exception ex) {
                logger.info("Remove user from cache failed. userName:{}, exMsg:{}", userName, ex.getMessage());
            }
            userRepository.setEnabled(userName, true);
        }
    }

    /**
     * 获取用户信息
     *
     * @param userName 用户名
     * @return 用户
     */
    public com.miaosu.flux.system.users.User get(String userName) {
        com.miaosu.flux.system.users.User user = null;
        try {
            user = userCache.get(userName, com.miaosu.flux.system.users.User.class);
        } catch (Exception ex) {
            logger.info("Get user from cache failed. userName:{}, exMsg:{}", userName, ex.getMessage());
        }

        if (user == null) {
            synchronized (this) {
                //  再次尝试从缓存获取
                try {
                    user = userCache.get(userName, com.miaosu.flux.system.users.User.class);
                    if (user != null) {
                        return user;
                    }
                } catch (Exception ex) {
                    logger.info("Get user from cache failed. userName:{}, exMsg:{}", userName, ex.getMessage());
                }

                // 从数据库获取
                user = userRepository.findOne(userName);

                if(user != null) {
                    // 不为空时，存入缓存
                    try {
                        userCache.put(userName, user);
                    } catch (Exception ex) {
                        logger.info("Put user to cache failed. userName:{}, exMsg:{}", userName, ex.getMessage());
                    }
                }
            }
        }
        return user;
    }

}
