package com.miaosu.flux.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.Serializable;

/**
 * 结果信息实体Bean
 * Created by angus on 15/5/27.
 */
@Data
@AllArgsConstructor
public class ResultInfo<T> implements Serializable{


    public static ResultInfo SUCCESSFUL() {
        return new ResultInfo(true);
    }

    public static ResultInfo FAILED() {
        return new ResultInfo(false);
    }

    private boolean success;

    /**
     * <pre>
     * 结果码，六位数字设计: ABCDEF；
     * AB代表系统，00：通用系统；
     * CD代表模块，00：通用模块；
     * EF代表结果，00：成功，01：失败；
     *
     * 示例：{@link ResultCode}
     * 000000 -> 表示所有系统所有模块操作成功；
     * 000001 -> 表示所有系统所有模块操作失败；
     * </pre>
     */
    private String code;

    private String message;

    private T data;

    public ResultInfo(boolean success) {
        this(success, success ? ResultCode.SUCCESSFUL : ResultCode.FAILED);
    }

    public ResultInfo(boolean success, String code) {
        this(success, code, MessageSourceUtil.getMessage(code, null, LocaleContextHolder.getLocale()));
    }

    public ResultInfo(boolean success, String code, String message){
        this(success, code, message, null);
    }

    public ResultInfo(boolean success, String code, T data) {
        this(success, code, MessageSourceUtil.getMessage(code, null, LocaleContextHolder.getLocale()), data);
    }
}
