package com.miaosu.flux.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AsyncProperties
 * Created by angus on 15/10/7.
 */
@ConfigurationProperties(prefix = "async")
@Data
public class AsyncProperties {

    /**
     * Set the ThreadPoolExecutor's core pool size.
     * Default is 2.
     */
    private int corePoolSize = 2;

    /**
     * Set the ThreadPoolExecutor's maximum pool size.
     * Default is 5.
     */
    private int maxPoolSize = 5;

    /**
     * Set the capacity for the ThreadPoolExecutor's BlockingQueue.
     * Default is 10000
     */
    private int queueCapacity = 10000;
}
