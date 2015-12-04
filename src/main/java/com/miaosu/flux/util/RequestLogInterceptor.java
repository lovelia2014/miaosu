//package com.miaosu.flux.util;
//
//import static net.logstash.logback.marker.Markers.append;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.MDC;
//import org.springframework.ui.ModelMap;
//import org.springframework.web.context.request.ServletWebRequest;
//import org.springframework.web.context.request.WebRequest;
//import org.springframework.web.context.request.WebRequestInterceptor;
//
//
///**
// * 请求日志拦截器
// * Created by angus on 15/6/15.
// */
//public class RequestLogInterceptor implements WebRequestInterceptor {
//
//    public static final String UUID_KEY = "uuId";
//
//    public static final String SESSION_ID_KEY = "sessionId";
//
//    public static final String INVOKE_TIME_KEY = "invokeTime";
//
//    public static final String BEGIN_TIME_KEY = "beginTime";
//
//    public static final String COST_TIME_KEY = "costTime";
//
//    public static final String METHOD_KEY = "methodName";
//
//    public static final String PARAMS_KEY = "paramValues";
//
//    public static final String RESULT_KEY = "resultValue";
//
//    public static final String EXCEPTION_MSG_KEY = "exceptionMsg";
//
//    public static final String REQUEST_REMOTE_HOST_MDC_KEY = "senderHost";
//
//    public static final String REQUEST_USER_AGENT_MDC_KEY = "userAgent";
//
//    public static final String REQUEST_REQUEST_URI = "req.requestURI";
//
//    public static final String REQUEST_QUERY_STRING = "req.queryString";
//
//    public static final String REQUEST_REQUEST_URL = "req.requestURL";
//
//    public static final String REQUEST_X_FORWARDED_FOR = "req.xForwardedFor";
//
//    public static final String REQUEST_REAL_IP = "req.xRealIp";
//
//    public static final String LOGED_FLAG = "$LOGED_FLAG$";
//
//    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
//
//    private Logger logger = LoggerFactory.getLogger("requestLog");
//
//    private void insertIntoMDC(HttpServletRequest request) {
//
//        MDC.put(INVOKE_TIME_KEY, new SimpleDateFormat(DATE_FORMAT_PATTERN).format(new Date()));
//        MDC.put(UUID_KEY, UUIDGen.systemUuid());
//        MDC.put(BEGIN_TIME_KEY, String.valueOf(System.currentTimeMillis()));
//        MDC.put(SESSION_ID_KEY, request.getRequestedSessionId());
//        MDC.put(METHOD_KEY, request.getMethod());
//        MDC.put(REQUEST_REMOTE_HOST_MDC_KEY, request.getRemoteHost());
//        MDC.put(REQUEST_REQUEST_URI, request.getRequestURI());
//        StringBuffer requestURL = request.getRequestURL();
//        if (requestURL != null) {
//            MDC.put(REQUEST_REQUEST_URL, requestURL.toString());
//        }
//        MDC.put(REQUEST_QUERY_STRING, request.getQueryString());
//        MDC.put(REQUEST_USER_AGENT_MDC_KEY, request.getHeader("User-Agent"));
//        MDC.put(REQUEST_X_FORWARDED_FOR, request.getHeader("X-Forwarded-For"));
//        MDC.put(REQUEST_REAL_IP, request.getHeader("X-Real-IP"));
//
//    }
//
//    private void clearMDC() {
//        MDC.remove(INVOKE_TIME_KEY);
//        MDC.remove(UUID_KEY);
//        MDC.remove(BEGIN_TIME_KEY);
//        MDC.remove(SESSION_ID_KEY);
//        MDC.remove(METHOD_KEY);
//        MDC.remove(COST_TIME_KEY);
//        MDC.remove(REQUEST_REMOTE_HOST_MDC_KEY);
//        MDC.remove(REQUEST_REQUEST_URI);
//        MDC.remove(REQUEST_QUERY_STRING);
//
//        // removing possibly inexistent item is OK
//        MDC.remove(REQUEST_REQUEST_URL);
//        MDC.remove(REQUEST_USER_AGENT_MDC_KEY);
//        MDC.remove(REQUEST_X_FORWARDED_FOR);
//        MDC.remove(REQUEST_REAL_IP);
//        MDC.remove(EXCEPTION_MSG_KEY);
//    }
//
//    private void calcCostTime(){
//        long costTime = -1l;
//        try {
//            costTime = System.currentTimeMillis() - Long.valueOf(MDC.get(BEGIN_TIME_KEY));
//        } catch (Exception e) {
//            // ignore any exception
//        }
//
//        MDC.put(COST_TIME_KEY, String.valueOf(costTime));
//    }
//
//
//    @Override
//    public void preHandle(WebRequest request) throws Exception {
//
//        if (request instanceof ServletWebRequest) {
//            insertIntoMDC(((ServletWebRequest) request).getRequest());
//        }
//    }
//
//    @Override
//    public void postHandle(WebRequest request, ModelMap model) throws Exception {
//        calcCostTime();
//
//        logger.info(append(PARAMS_KEY, request.getParameterMap()).and(append(RESULT_KEY, model)), "");
//
//        MDC.put(LOGED_FLAG, "true");
//    }
//
//    @Override
//    public void afterCompletion(WebRequest request, Exception ex) throws Exception {
//        if (ex != null || MDC.get(LOGED_FLAG) == null) {
//            calcCostTime();
//
//            if (ex != null) {
//                MDC.put(EXCEPTION_MSG_KEY, ex.getLocalizedMessage());
//            }
//            logger.error(append(PARAMS_KEY, request.getParameterMap()), "request fail.");
//        }
//        clearMDC();
//    }
//}
