package com.miaosu.flux.openapi;

import com.miaosu.flux.base.ResultCode;
import com.miaosu.flux.base.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 开放控制器基类
 * Created by angus on 15/10/4.
 */
public class OpenBaseController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void checkSign(Map<String, Object> requestParam, String sign, String secret) {
        if (sign == null || !sign.equals(OpenSign.getSign(requestParam, secret))) {
            logger.error("签名校验失败, secret:{}", secret);
            throw new ServiceException(ResultCode.OPEN_SIGN_ERROR);
        }
    }
}
