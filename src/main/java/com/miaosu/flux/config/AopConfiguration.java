package com.miaosu.flux.config;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.interceptor.CustomizableTraceInterceptor;
import org.springframework.aop.interceptor.PerformanceMonitorInterceptor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;

/**
 * 日志与性能AOP，日志级别设置为TRACE时可以查看；
 * Created by angus on 15/10/9.
 */
@Configuration
@EnableAspectJAutoProxy
@Aspect
public class AopConfiguration {

    /** Pointcut for execution of methods on {@link Service} annotation */
    @Pointcut("execution(public * (@org.springframework.stereotype.Service com.miaosu..*).*(..))")
    public void serviceAnnotation(){}

    /** Pointcut for execution of methods on {@link Repository} annotation */
    @Pointcut("execution(public * (@org.springframework.stereotype.Repository com.miaosu..*).*(..))")
    public void repositoryAnnotation(){}

    /** Pointcut for execution of methods on {@link JpaRepository} interfaces */
    @Pointcut("execution(public * org.springframework.data.jpa.repository.JpaRepository+.*(..))")
    public void japRepositoryAnnotation(){}

    @Pointcut("serviceAnnotation() || repositoryAnnotation() || japRepositoryAnnotation()")
    public void performanceMonitor() {}

    @Pointcut("serviceAnnotation() || repositoryAnnotation() || japRepositoryAnnotation()")
    public void logPoint(){}

    @Bean
    public CustomizableTraceInterceptor traceInterceptor() {
        CustomizableTraceInterceptor interceptor = new CustomizableTraceInterceptor();
        interceptor.setEnterMessage("Entering $[methodName], params:[$[arguments]]");
        interceptor.setExitMessage("Leaving $[methodName], result:[$[returnValue]], took: $[invocationTime]ms");
        interceptor.setExceptionMessage("Exception in $[methodName], cause:$[exception]");
        interceptor.setUseDynamicLogger(true);
        return interceptor;
    }

    @Bean
    public Advisor traceAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("com.miaosu.flux.config.AopConfiguration.logPoint()");

        return new DefaultPointcutAdvisor(pointcut, traceInterceptor());
    }

    @Bean
    public PerformanceMonitorInterceptor performanceMonitorInterceptor() {
        return new PerformanceMonitorInterceptor(true);
    }

    @Bean
    public Advisor performanceMonitorAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("com.miaosu.flux.config.AopConfiguration.performanceMonitor()");
        return new DefaultPointcutAdvisor(pointcut, performanceMonitorInterceptor());
    }

}
