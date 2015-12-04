package com.miaosu.flux.config;

import com.miaosu.flux.util.RequestLogFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.EnumSet;

/**
 * web配置
 * Created by angus on 15/10/13.
 */
@Configuration
public class WebConfiguration  extends WebMvcConfigurerAdapter implements ServletContextInitializer {
    private final Logger log = LoggerFactory.getLogger(WebConfiguration.class);

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        initRequestLogFilter(servletContext);
        log.info("Web application fully configured");
    }

    public void initRequestLogFilter(ServletContext servletContext) {
        FilterRegistration.Dynamic teeFilter = servletContext.addFilter("teeFilter", new RequestLogFilter());
        EnumSet<DispatcherType> dispatcherTypes = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC);
        teeFilter.addMappingForUrlPatterns(dispatcherTypes, true, "/api/*", "/openapi/*");
    }
}
