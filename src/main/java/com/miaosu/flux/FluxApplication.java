package com.miaosu.flux;

import java.io.IOException;
import java.util.Locale;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

import com.miaosu.flux.base.MessageSourceUtil;
import com.miaosu.flux.config.security.ApplicationSecurity;

/**
 * 启动类
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableSwagger2
public class FluxApplication extends WebMvcConfigurerAdapter implements InitializingBean{

    /**
     * 视图配置
     * @param registry ViewControllerRegistry
     */
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/welcome").setViewName("welcome");
    }

    /**
     * 拦截器配置
     * @param registry InterceptorRegistry
     */
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addWebRequestInterceptor(new RequestLogInterceptor()).excludePathPatterns("/css/**", "/extjs/**", "/images/**", "/js/**", "*.js", "*.css");
//        registry.addWebRequestInterceptor(new RequestLogInterceptor()).addPathPatterns("/api/**", "/notify/**", "/openapi/**");
    }

    /**
     * 应用访问安全控制
     * @return ApplicationSecurity
     */
    @Bean
    public ApplicationSecurity applicationSecurity() {
        return new ApplicationSecurity();
    }

    @Autowired
    private DataSource dataSource;

    /**
     * 用户管理
     * @return UserDetailsManager
     */
    @Bean( autowire = Autowire.BY_TYPE)
    public UserDetailsManager userDetailsManager() {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();
        jdbcUserDetailsManager.setDataSource(dataSource);
        jdbcUserDetailsManager.setEnableGroups(true);
        return jdbcUserDetailsManager;
    }

    /**
     * RememberMeServices数据源
     * @return JdbcTokenRepositoryImpl
     */
    @Bean
    public PersistentTokenRepository jdbcTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }

    /**
     * 密码加密器
     * @return PasswordEncoder
     */
    @SuppressWarnings("deprecation")
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new ShaPasswordEncoder(256);
    }

    /**
     * 本地化设置
     * @return
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.CHINA);
        return slr;
    }

    @Autowired
    private MessageSource messageSource;

    public static void main(String[] args) throws IOException {
        SpringApplication.run(FluxApplication.class, args);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageSourceUtil.init(messageSource);
    }
}
