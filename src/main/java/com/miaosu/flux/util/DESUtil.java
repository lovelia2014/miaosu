package com.miaosu.flux.util;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.SecureRandom;

/**
 * DES工具类
 */
public class DESUtil {

    private static Logger logger = LoggerFactory.getLogger(DESUtil.class);

    public static String encryptToString(String base64Str, String password) {
        byte[] result = encrypt(Base64.decodeBase64(base64Str), password);
        return Base64.encodeBase64String(result);
    }

    public static String decryptToString(String base64Str, String password) {
        byte[] result = decrypt(Base64.decodeBase64(base64Str), password);
        return Base64.encodeBase64String(result);
    }

    /**
     * 加密
     * @param datasource byte[]
     * @param password String
     * @return byte[]
     */
    public static byte[] encrypt(byte[] datasource, String password) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(password.getBytes());
            // 创建一个密匙工厂，然后用它把DESKeySpec转换成
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            // Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance("DES");
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
            // 现在，获取数据并加密
            // 正式执行加密操作
            return cipher.doFinal(datasource);
        } catch (Throwable e) {
            logger.error("加密字符串出错; datasource:{}, password:{}", Base64.encodeBase64String(datasource), password, e);
        }
        return null;
    }

    /**
     * 解密
     * @param src byte[]
     * @param password String
     * @return byte[]
     * @throws Exception
     */
    public static byte[] decrypt(byte[] src, String password) {
        try {
            // DES算法要求有一个可信任的随机数源
            SecureRandom random = new SecureRandom();
            // 创建一个DESKeySpec对象
            DESKeySpec desKey = new DESKeySpec(password.getBytes());
            // 创建一个密匙工厂
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            // 将DESKeySpec对象转换成SecretKey对象
            SecretKey securekey = keyFactory.generateSecret(desKey);
            // Cipher对象实际完成解密操作
            Cipher cipher = Cipher.getInstance("DES");
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, securekey, random);
            // 真正开始解密操作
            return cipher.doFinal(src);

        } catch (Throwable e) {
            logger.error("解密字符串出错; datasource:{}, password:{}", Base64.encodeBase64String(src), password, e);
        }
        return null;
    }

//    public static void main(String[] args) throws Exception {
//        String password = "MS000001";
//        String content = Base64.encodeBase64String(UUIDGen.systemUuid().getBytes());
//        System.out.println("加密前：" + content);
//
//        String result = encryptToString(content, password);
//        System.out.println("加密后：" + result);
//
//        System.out.println("解密后：" + decryptToString(result, password));MDBkNjM2ZmEtMDQ3Ni00NzE0LWFkZjEtYThkNWM3MGUwOTcx
//        System.out.println("解密后：" + decryptToString("ULUlCplFPPU9Rj/hKJy3WXbSBhljkTE3OY7tSPMEbgXCZjezFzvwmw==", "wh_yunliu"));
//    }
}