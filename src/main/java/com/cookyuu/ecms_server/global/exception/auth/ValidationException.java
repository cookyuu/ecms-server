package com.cookyuu.ecms_server.global.exception.auth;

import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ValidateFormatException extends ECMSAppException {
    public ValidateFormatException() {
        super(ResultCode.INVALID_INPUT_VALUE);
    }
    public ValidateFormatException(ResultCode resultCode) {
        super(resultCode);
    }
}
