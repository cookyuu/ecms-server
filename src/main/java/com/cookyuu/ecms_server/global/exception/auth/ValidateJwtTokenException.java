package com.cookyuu.ecms_server.global.exception.auth.jwt;

import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ValidateJwtTokenException extends ECMSAppException {
    public ValidateJwtTokenException() {
        super(ResultCode.INVALID_INPUT_VALUE);
    }
    public ValidateJwtTokenException(ResultCode resultCode) {
        super(resultCode);
    }
}
