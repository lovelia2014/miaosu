package com.miaosu.flux.supplier.huazong.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * 华众结果码信息
 * Created by angus on 15/10/8.
 */
public class HZResultCode {
    private static Map<String, String> codeMap = new HashMap<>();

    static {
        codeMap.put("0", "成功");
        codeMap.put("1001", "缺少接口方法参数!");
        codeMap.put("1002", "接口方法不存在");
        codeMap.put("1003", "服务器内部错误");
        codeMap.put("1004", "缺少用户名参数");
        codeMap.put("1005", "用户名或密码不正确");
        codeMap.put("1006", "余额不足");
        codeMap.put("1007", "缺少应用参数");
        codeMap.put("1008", "方法不存在");
        codeMap.put("1009", "订购的产品不存在");
        codeMap.put("1010", "Sign错误");
        codeMap.put("1011", "查询的订单号不存在");
    }

    public static String getMsg(String code) {
        return codeMap.get(code);
    }
}
