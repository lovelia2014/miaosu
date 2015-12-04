package com.miaosu.flux.supplier.huazong;

import com.miaosu.flux.util.MD5Util;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * 华众签名算法工具类
 * Created by angus on 15/10/8.
 */
public final class HuaZongSign {

    public static String getSign(Map<String, Object> requestParam, String secret) {
        Map<String, Object> treeMap = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        treeMap.putAll(requestParam);

        // 过滤掉值为空的参数
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : treeMap.entrySet()) {
            if (entry.getValue() != null) {
                if("".equals(entry.getValue())){
                    continue;
                }
                sb.append(entry.getKey()).append(entry.getValue());
            }
        }

        sb.append(secret);

        return MD5Util.computeMD5(sb.toString());
    }


//
//    public static void main(String[] args) throws UnsupportedEncodingException {
//        Map<String, Object> paramMap = new HashMap<>();
//        paramMap.put("UserId", "A0001");
//        paramMap.put("Method", "OrderFlow");
//        paramMap.put("Telephone", "13812345678");
//        paramMap.put("EffectType", 0);
//        paramMap.put("ProductCode", "P000001");
//        paramMap.put("TransNo", "T00000001");
//        paramMap.put("PhoneProvince", "江苏");
//        paramMap.put("NotifyUrl", "http://127.0.0.1/notify.html");
//
//        System.out.println(getSign(paramMap, "9c6d7b828f66eec8a449faba93a10e43"));
//    }
}
