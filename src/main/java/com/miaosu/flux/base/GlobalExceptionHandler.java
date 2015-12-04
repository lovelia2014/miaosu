package com.miaosu.flux.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理
 * Created by angus on 15/5/27.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResultInfo defaultErrorHandler(HttpServletRequest request, Exception e)
            throws Exception {
        // If the exception is annotated with @ResponseStatus rethrow it and let
        // the framework handle it - like the OrderNotFoundException example
        // at the start of this post.
        // AnnotationUtils is a Spring Framework utility class.
        if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null)
            throw e;

        if(logger.isDebugEnabled()) {
            logger.error("access [{}] failed.", request.getRequestURL(), e);
        }else{
            logger.error("access [{}] failed. exMsg:{}", request.getRequestURL(), e.getLocalizedMessage());
        }

        return new ResultInfo(false, ResultCode.FAILED, e.getLocalizedMessage());
    }

//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = ServiceException.class)
    @ResponseBody
    public ResultInfo serviceExceptionHandler(HttpServletRequest request, ServiceException e)
            throws Exception {
        // If the exception is annotated with @ResponseStatus rethrow it and let
        // the framework handle it - like the OrderNotFoundException example
        // at the start of this post.
        // AnnotationUtils is a Spring Framework utility class.
        if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null)
            throw e;

        if(logger.isDebugEnabled()) {
            logger.error("access [{}] failed.", request.getRequestURL(), e);
        }else{
            logger.error("access [{}] failed. exMsg:{}", request.getRequestURL(), e.getLocalizedMessage());
        }

        return new ResultInfo(false, e.getErrorCode(), e.getLocalizedMessage());
    }
}
