package com.cookyuu.ecms_server.global.exception.auth;

import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ValidationException extends ECMSAppException {
    public ValidationException() {
        super(ResultCode.INVALID_INPUT_VALUE);
    }
    public ValidationException(ResultCode resultCode) {
        super(resultCode);
    }
}
