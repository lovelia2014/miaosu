package com.miaosu.flux.recharge.task;

import com.miaosu.flux.members.Member;
import com.miaosu.flux.members.MemberService;
import com.miaosu.flux.openapi.OpenSign;
import com.miaosu.flux.util.DESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 回调任务，充值成功后，通知外部系统
 * Created by angus on 15/10/11.
 */
@Component
public class CallbackTask implements InitializingBean, DisposableBean {

    private static final Logger callbackLogger = LoggerFactory.getLogger("callback");

    public static BlockingQueue<Callback> QUEUE = new ArrayBlockingQueue<>(10000);

    @Autowired
    @Qualifier("callbackRestTemplate")
    private RestTemplate callbackRestTemplate;

    @Autowired
    private MemberService memberService;

    @Autowired
    private AsyncTaskExecutor asynTaskExecutor;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private boolean runnable = false;

    public void execute() {
        while (runnable) {
            try {
                final Callback callback = QUEUE.take();

                asynTaskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        doCallback(callback);
                    }
                });
            } catch (Exception e) {
                callbackLogger.warn("回调任务发生异常", e);
            }

        }
    }

    /**
     * 执行回调
     */
    public void doCallback(Callback callback) {
        long begin = System.currentTimeMillis();

        Map<String, Object> paramMap = new HashMap<>();
        String notifyUrl = callback.getNotifyUrl();
        String result = null;
        try {
            // 回调地址不为空才执行回调；
            if (!StringUtils.hasText(notifyUrl)) {
                return;
            }

            // 获取用户密钥
            String secret = getSecret(callback.getUserName());
            if (secret == null) {
                return;
            }

            paramMap.put("orderId", callback.getOrderId());
            paramMap.put("transId", callback.getTransId());
            paramMap.put("status", callback.getRechargeState().ordinal());
            paramMap.put("failedReason", callback.getFailedReason());
            String sign = OpenSign.getSign(paramMap, secret);
            paramMap.put("sign", sign);

            result = callbackRestTemplate.getForObject(
                    notifyUrl + "?orderId={orderId}&transId={transId}&status={status}&failedReason={failedReason}&sign={sign}",
                    String.class, paramMap);

        } catch (Exception ex) {
            callbackLogger.warn("回调失败; notifyUrl:{}, paramMap:{}, exMsg:{}", notifyUrl, paramMap, ex.getMessage());
        } finally {
            callbackLogger.info("回调结束; notifyUrl:{}, paramMap:{}; result:{}; costTime:{}", notifyUrl, paramMap, result,
                    (System.currentTimeMillis() - begin));
        }
    }
    
    public static void main(String[] args)
	{
    	Map<String, Object> paramMap = new HashMap<>();
    	paramMap.put("orderId", "D000000000913182");
        paramMap.put("transId", "20151203155117187130810888661617");
        paramMap.put("status", 3);
        paramMap.put("failedReason", "8888:产品200M全国流量订购失败。失败描述：[INDETERMINATE]Utility.cpp:207,CRMException-300263: TCS_ChangeServiceReg执行异常:[INDETERMINATE]TradeCheckTradeSuperLimit.cpp:403,CRMException-300263: 特殊限制判断:用户必选3种服务EDGE+服务,接入2G网络,3G上网服务 中的一个才能继续受理业务!");
        String sign = OpenSign.getSign(paramMap, DESUtil.decryptToString("6fugMFC5BEB92yolInanfVN0kZ7ztdAXmMghhv6j7dBW1DIxbSYX9Q==", "sd_yafeida"));
        System.out.println(sign);
	}
    
    /**
     * 获取用户密钥
     * @param userName 用户名
     * @return 密钥
     */
    private String getSecret(String userName) {
        try {
            Member member = memberService.get(userName);
            return DESUtil.decryptToString(member.getToken(), userName);
        } catch (Exception ex) {
            callbackLogger.warn("获取用户密钥失败，userName:{}, exMsg:{}", userName, ex.getMessage());
            return null;
        }
    }

    @Override
    public void destroy() throws Exception {
        this.runnable = false;
        executor.shutdown();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.runnable = true;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                execute();
            }
        });
    }
}
