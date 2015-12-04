package com.miaosu.flux.supplier.huazong;

import com.miaosu.flux.base.ResultCode;
import com.miaosu.flux.base.ServiceException;
import com.miaosu.flux.supplier.huazong.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 华众平台
 * Created by angus on 15/10/6.
 */
@Component
public class HuaZongPlatform {
    private static Logger hzLogger = LoggerFactory.getLogger("recharge");

    private static final String REQUEST_PATH = "/s/ares.php";

    private static final String NOTIFY_PATH = "/notify/orderstatus";

    @Value("${huazong.secret}")
    private String secret = "";

    @Value("${huazong.userId}")
    private String userId = "";

    @Value("${huazong.serverUrl}")
    private String serverUrl;

    @Value("${huazong.notifyUrl}")
    private String notifyUrl;

    @Autowired
    @Qualifier("defaultRestTemplate")
    private RestTemplate restTemplate;

    /**
     * 校验订购接口
     * @param phone 号码
     * @param productId 商品代码
     * @return 校验结果
     */
    public boolean validate(String phone, String productId) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("UserId", userId);
        paramMap.put("Method", "CheckOrderFlow");
        paramMap.put("Telephone", phone);
        paramMap.put("ProductCode", productId);
        String sign = HuaZongSign.getSign(paramMap, secret);
        paramMap.put("Sign", sign);

        long begin = System.currentTimeMillis();
        CheckOrderFlowResult result = null;
        try {
            result = restTemplate.getForObject(
                    serverUrl + REQUEST_PATH
                            + "?UserId={UserId}&Method={Method}&Telephone={Telephone}&ProductCode={ProductCode}&Sign={Sign}",
                    CheckOrderFlowResult.class, paramMap);
        } catch (Exception ex) {
            hzLogger.warn("校验订购失败, paramMap:{}, exMsg:{}; costTime:{}", paramMap, ex.getMessage(), (System.currentTimeMillis() - begin));
            throw new ServiceException(ResultCode.FAILED);
        } finally {
            hzLogger.info("校验订购结束; paramMap:{}; result:{}; costTime:{}", paramMap, result, (System.currentTimeMillis() - begin));
        }

        if (result != null) {
            if ("0".equals(result.getCode()) && "Y".equals(result.getStatus())) {
                return true;
            } else {
                throw new ServiceException(ResultCode.FAILED, result.getFailedReason());
            }
        } else {
            hzLogger.warn("校验订购返回结果为null");
            throw new ServiceException(ResultCode.FAILED);
        }
    }

    /**
     * 订购接口
     * @param phone 号码
     * @param effectType 0：立即生效 1：下月生效
     * @param productId 商品代码
     * @param orderId 订单编号
     * @param province 省份
     * @return 订单结果
     */
    public OrderFlowResult order(String phone, int effectType, String productId, String orderId, String province) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("UserId", userId);
        paramMap.put("Method", "OrderFlow");
        paramMap.put("Telephone", phone);
        paramMap.put("EffectType", effectType);
        paramMap.put("ProductCode", productId);
        paramMap.put("TransNo", orderId);
        paramMap.put("PhoneProvince", province);
        paramMap.put("NotifyUrl", notifyUrl + NOTIFY_PATH);
        String sign = HuaZongSign.getSign(paramMap, secret);
        paramMap.put("Sign", sign);

        long begin = System.currentTimeMillis();
        OrderFlowResult result = null;
        try {
            result = restTemplate.getForObject(
                    serverUrl + REQUEST_PATH
                            + "?UserId={UserId}&Method={Method}&Telephone={Telephone}&EffectType={EffectType}&"
                            + "ProductCode={ProductCode}&TransNo={TransNo}&PhoneProvince={PhoneProvince}&NotifyUrl={NotifyUrl}&Sign={Sign}",
                    OrderFlowResult.class, paramMap);
        } catch (Exception ex) {
            hzLogger.warn("订购失败, paramMap:{}, exMsg:{}; costTime:{}", paramMap, ex.getMessage(), (System.currentTimeMillis() - begin));
            throw new ServiceException(ResultCode.FAILED);
        } finally {
            hzLogger.info("订购结束; paramMap:{}; result:{}; costTime:{}", paramMap, result, (System.currentTimeMillis() - begin));
        }

        if (result != null) {
            return result;
        } else {
            hzLogger.warn("订购返回结果为null");
            throw new ServiceException(ResultCode.FAILED);
        }
    }

    /**
     * 订购状态查询接口
     * @param orderId 订单编号
     * @param rechargeId 充值订单编号
     * @return 订单状态结果（Y:订购成功,N:订购失败,P:订购中)
     */
    public GetOrderStatusResult queryOrderStatus(String orderId, String rechargeId) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("UserId", userId);
        paramMap.put("Method", "GetOrderStatus");
        paramMap.put("OrderNo", rechargeId);
        paramMap.put("TransNo", orderId);
        String sign = HuaZongSign.getSign(paramMap, secret);
        paramMap.put("Sign", sign);

        long begin = System.currentTimeMillis();
        GetOrderStatusResult result = null;
        try {
            result = restTemplate.getForObject(
                    serverUrl + REQUEST_PATH
                            + "?UserId={UserId}&Method={Method}&OrderNo={OrderNo}&TransNo={TransNo}&Sign={Sign}",
                    GetOrderStatusResult.class, paramMap);
        } catch (Exception ex) {
            hzLogger.warn("查询订单失败, paramMap:{}, exMsg:{}; costTime:{}", paramMap, ex.getMessage(), (System.currentTimeMillis() - begin));
            throw new ServiceException(ResultCode.FAILED);
        } finally {
            hzLogger.info("查询订单结束; paramMap:{}; result:{}; costTime:{}", paramMap, result, (System.currentTimeMillis() - begin));
        }

        if (result != null) {
            if ("0".equals(result.getCode())) {
                return result;
            } else if("1011".equals(result.getCode())) {
                throw new ServiceException(ResultCode.DATA_NOT_EXISTS);
            } else {
                throw new ServiceException(ResultCode.FAILED, result.getFailedReason());
            }
        } else {
            hzLogger.warn("查询订单返回结果为null");
            throw new ServiceException(ResultCode.FAILED);
        }
    }

    /**
     * 余额查询接口
     * @return 用户余额
     */
    public String queryBalance() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("UserId", userId);
        paramMap.put("Method", "GetAccountInfo");
        String sign = HuaZongSign.getSign(paramMap, secret);
        paramMap.put("Sign", sign);

        long begin = System.currentTimeMillis();
        GetAccountInfoResult result = null;
        try {
            result = restTemplate.getForObject(
                    serverUrl + REQUEST_PATH + "?UserId={UserId}&Method={Method}&Sign={Sign}",
                    GetAccountInfoResult.class, paramMap);
        } catch (Exception ex) {
            hzLogger.warn("查询余额失败, paramMap:{}, exMsg:{}; costTime:{}", paramMap, ex.getMessage(), (System.currentTimeMillis() - begin));
            throw new ServiceException(ResultCode.FAILED);
        } finally {
            hzLogger.info("查询余额结束; paramMap:{}; result:{}; costTime:{}", paramMap, result, (System.currentTimeMillis() - begin));
        }

        if (result != null) {
            if ("0".equals(result.getCode())) {
                return result.getAccountBalance();
            } else {
                throw new ServiceException(ResultCode.FAILED);
            }
        } else {
            hzLogger.warn("查询余额返回结果为null");
            throw new ServiceException(ResultCode.FAILED);
        }
    }

    /**
     * 商品列表查询接口
     * @return 用户余额
     */
    public GetProductListResult queryProductList() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("UserId", userId);
        paramMap.put("Method", "GetProductList");
        String sign = HuaZongSign.getSign(paramMap, secret);
        paramMap.put("Sign", sign);

        long begin = System.currentTimeMillis();
        GetProductListResult result = null;
        try {
            result = restTemplate.getForObject(
                    serverUrl + REQUEST_PATH + "?UserId={UserId}&Method={Method}&Sign={Sign}",
                    GetProductListResult.class, paramMap);
        } catch (Exception ex) {
            hzLogger.warn("商品列表查询失败, paramMap:{}, exMsg:{}; costTime:{}", paramMap, ex.getMessage(), (System.currentTimeMillis() - begin));
            throw new ServiceException(ResultCode.FAILED);
        } finally {
            hzLogger.info("商品列表查询结束; paramMap:{}; result:{}; costTime:{}", paramMap, result, (System.currentTimeMillis() - begin));
        }

        return result;

        // if (result != null) {
        // if (result.getCode() == 0) {
        // return result.getAccountBalance();
        // } else {
        // throw new ServiceException(ResultCode.FAILED);
        // }
        // } else {
        // hzLogger.warn("商品列表查询返回结果为null");
        // throw new ServiceException(ResultCode.FAILED);
        // }
    }
}
