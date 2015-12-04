package com.miaosu.flux.base;

/**
 * 结果码
 * Created by angus on 15/9/29.
 */
public final class ResultCode {

    /**
     * 操作成功；
     */
    public static String SUCCESSFUL = "000000";

    /**
     * 操作失败；
     */
    public static String FAILED = "000001";

    /**
     * 禁止访问；
     */
    public static String ACCESS_DENIED = "000002";

    /**
     * 参数错误
     */
    public static String PARAM_WRONG = "000003";

    /**
     * 数据已存在
     */
    public static String DATA_EXISTS = "000004";

    /**
     * 数据不存在
     */
    public static String DATA_NOT_EXISTS = "000005";

    /**
     * 数据不合法，违反约束
     */
    public static String DATA_CONSTRAINT_VIOLATION = "000006";

    /**
     * 修改密码时，验证旧密码失败；
     */
    public static String  CHANGE_PWD_WITH_WORNG_OLD_PWD = "000101";

    /**
     * 开放接口——用户不存在或被禁用
     */
    public static String OPEN_USER_NOT_EXISTS = "010001";

    /**
     * 开放接口——签名校验异常
     */
    public static String OPEN_SIGN_ERROR = "010002";

    /**
     * 开放接口——商品不存在或已下架
     */
    public static String OPEN_PRODUCT_NOT_EXISTS = "010003";

    /**
     * 开放接口——余额不足
     */
    public static String OPEN_NO_BALANCE = "010004";

    /**
     * 开放接口——订单不存在
     */
    public static String OPEN_ORDER_NOT_EXISTS = "010005";
}
