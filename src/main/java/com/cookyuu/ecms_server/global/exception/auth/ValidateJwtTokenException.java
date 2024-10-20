package com.cookyuu.ecms_server.global.exception.auth;

import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ValidateJwtTokenException extends ECMSAppException {
    public ValidateJwtTokenException() {
        super(ResultCode.BAD_REQUEST);
    }
    public ValidateJwtTokenException(ResultCode resultCode) {
        super(resultCode);
    }
}
