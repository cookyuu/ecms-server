package com.cookyuu.ecms_server.global.exception.utils;

import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class EcmsRedissonException extends ECMSAppException {
    public EcmsRedissonException() {
        super(ResultCode.REDISSON_COMMON_EXP);
    }
    public EcmsRedissonException(ResultCode resultCode) {
        super(resultCode);
    }

    public EcmsRedissonException(Throwable t) {
        super(ResultCode.REDIS_COMMON_EXP, t);
    }
}
