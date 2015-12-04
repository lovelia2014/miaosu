package com.miaosu.flux.config;

import static springfox.documentation.builders.PathSelectors.regex;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * ApiDoc自动配置类
 * Created by angus on 15/9/7.
 */
@Configuration
@EnableConfigurationProperties(ApiDocProperties.class)
@ConditionalOnClass(Docket.class)
public class ApiDocAutoConfiguration {

    @Autowired
    private ApiDocProperties apiDocProperties;

    @Bean
    public Docket petApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .pathMapping("/")
                .genericModelSubstitutes(ResponseEntity.class)
                .directModelSubstitute(Date.class, String.class)
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(regex(apiDocProperties.getIncludePattern()))
                .build()
                ;
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(apiDocProperties.getTitle(),
                apiDocProperties.getDescription(),
                apiDocProperties.getVersion(),
                apiDocProperties.getTermOfServiceUrl(),
                apiDocProperties.getContact(),
                apiDocProperties.getLicense(),
                apiDocProperties.getLicenseUrl());
    }

}
