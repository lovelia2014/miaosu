package com.miaosu.flux.config;

import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * ehcache缓存配置
 * Created by angus on 15/10/21.
 */
@Configuration
public class EhcacheConfiguration {
    private static final String ORDER_RECHARGE_STATE_CACHE_NAME = "OrderRechargeState";

    private static final String MEMBER_CACHE_NAME = "Member";

    private static final String USER_CACHE_NAME = "User";

    @Bean
    public Cache memberCache(EhCacheCacheManager ehCacheCacheManager) {
        return ehCacheCacheManager.getCache(MEMBER_CACHE_NAME);
    }

    @Bean
    public Cache userCache(EhCacheCacheManager ehCacheCacheManager) {
        return ehCacheCacheManager.getCache(USER_CACHE_NAME);
    }

    @Bean
    public Cache orderRechargeStateCache(EhCacheCacheManager ehCacheCacheManager) {
        return ehCacheCacheManager.getCache(ORDER_RECHARGE_STATE_CACHE_NAME);
    }

    @Bean
    public EhCacheCacheManager ehCacheCacheManager(EhCacheManagerFactoryBean bean) {
        return new EhCacheCacheManager(bean.getObject());
    }

    @Bean
    public EhCacheManagerFactoryBean ehcacheManagerFactoryBean() {
        EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        cacheManagerFactoryBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
        cacheManagerFactoryBean.setShared(true);

        return cacheManagerFactoryBean;
    }
}
