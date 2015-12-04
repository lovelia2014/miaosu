package com.miaosu.flux.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Config properties for ApiDoc
 * Created by angus on 15/9/7.
 */
@ConfigurationProperties(prefix = "api-doc")
@Data
public class ApiDocProperties {
    /**
     * API文档标题
     */
    private String title = "Api Documentation";

    /**
     * API文档描述
     */
    private String description = "Api Documentation";

    /**
     * API文档版本
     */
    private String version = "1.0";

    private String termOfServiceUrl = "urn:tos";

    /**
     * 联系方式
     */
    private String contact = "Contact Email";

    /**
     * license限制
     */
    private String license = "Apache 2.0";

    /**
     * license限制详细地址
     */
    private String licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0";

    /**
     * API文档的包含路径，可配正则表达式，默认包含所有；
     */
    private String includePattern = ".*";
}
