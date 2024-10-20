package com.cookyuu.ecms_server.global.exception.utils;

import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class EcmsRedisException extends ECMSAppException {
    public EcmsRedisException() {
        super(ResultCode.REDIS_COMMON_EXP);
    }
    public EcmsRedisException(ResultCode resultCode) {
        super(resultCode);
    }

    public EcmsRedisException(Throwable t) {
        super(ResultCode.REDIS_COMMON_EXP, t);
    }
}
