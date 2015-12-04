package com.miaosu.flux.openapi;

import com.miaosu.flux.util.MD5Util;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * 签名算法
 * Created by angus on 15/10/11.
 */
public final class OpenSign {

    public static String getSign(Map<String, Object> requestParam, String secret){
        Map<String,Object> treeMap = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        treeMap.putAll(requestParam);

        // 过滤掉值为空的参数
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, Object> entry: treeMap.entrySet()){
            if(entry.getValue() != null) {
                if("".equals(entry.getValue())){
                    continue;
                }
                sb.append(entry.getKey()).append(entry.getValue());
            }
        }

        sb.append(secret);

        return MD5Util.computeMD5(sb.toString());
    }
}
