package com.miaosu.flux.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 序列号工具类
 * Created by angus on 15/9/29.
 */
@Component
public class SerialNoUtil {
    private static Logger logger = LoggerFactory.getLogger(SerialNoUtil.class);

    @Autowired
    private SerialNoService serialNoService;

    /**
     * 生成会员名
     * @return 格式为MS000001的字符串
     */
    public String genrateMemberName() {
        return generateSerialNo("seq_member_no", "MS", '0', 6);
    }

    /**
     * 生成账单号
     * @return 格式为MS000001的字符串
     */
    public String genrateBillNo() {
        return generateSerialNo("seq_bill_no", "B", '0', 15);
    }

    /**
     * 生成订单号
     * @return 格式为MS000001的字符串
     */
    public String genrateOrderNo() {
        return generateSerialNo("seq_order_no", "D", '0', 15);
    }

    /**
     * 生成序列号，譬如：传递参数("MS", '0', 5)，返回：MS00001
     * @param seqName 序列名
     * @param prefix 前缀
     * @param ch 补位字符
     * @param length 补位长度
     * @return 序列号
     */
    private String generateSerialNo(String seqName, String prefix, char ch, int length) {
        try {
            Long id = serialNoService.nextVal(seqName);
            return String.format(prefix + "%" + ch + length + "d", id);
        }catch(Exception ex){
            logger.warn("generate serial no failed", ex);
        }
        return null;
    }
//
//    public static void main(String[] args) {
//        System.out.println(String.format("%" + '0' + 5 + "d", 1));
//        System.out.println(String.format("%" + '0' + 5 + "d", 12345));
//        System.out.println(String.format("%" + '0' + 5 + "d", 123456));
//    }
}
