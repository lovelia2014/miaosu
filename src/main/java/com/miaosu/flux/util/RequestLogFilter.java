package com.miaosu.flux.util;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * http请求日志记录
 * Created by angus on 15/10/13.
 */
public class RequestLogFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RequestLogFilter.class);

    private static final Logger requestLog = LoggerFactory.getLogger("requestLog");

    private static com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    static {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        if (request instanceof HttpServletRequest) {
            Long startTimeMills = System.currentTimeMillis();

            if (response.getCharacterEncoding() == null) {
                response.setCharacterEncoding("UTF-8"); // Or whatever default. UTF-8 is good for World Domination.
            }

            Map<String, Object> logMap = new HashMap<>();

            HttpServletRequest httpRequsst = (HttpServletRequest) request;

            logMap.put("params", httpRequsst.getParameterMap());
            logMap.put("method", httpRequsst.getMethod());
            String realIp = httpRequsst.getHeader("X-Real-IP");
            realIp = realIp == null ? request.getRemoteHost() : realIp;
            logMap.put("realIp", realIp);
            logMap.put("requestUri", httpRequsst.getRequestURI());

            HttpServletResponseCopier responseCopier = null;

            try {
                responseCopier = new HttpServletResponseCopier((HttpServletResponse) response);
                chain.doFilter(request, responseCopier);
            } finally {
                if(httpRequsst.getRequestURI().contains("/openapi")) { //仅openapi记录响应结果
                    try {
                        String result = getOutputParamJsonStr(response, responseCopier);
                        logMap.put("result", result);
                    } catch (Exception e) {
                        // ignore
                        logger.warn("http接口日志参数封装失败", e);
                    }
                }
                long costTime = System.currentTimeMillis() - startTimeMills;
                logMap.put("costTime", costTime);

                requestLog.info(objectMapper.writeValueAsString(logMap));
            }
        }
    }

    private String getOutputParamJsonStr(ServletResponse response, HttpServletResponseCopier responseCopier)
            throws Exception {
        responseCopier.flushBuffer();
        byte[] copy = responseCopier.getCopy();
        return new String(copy, response.getCharacterEncoding());
    }

    @Override
    public void destroy() {

    }

}