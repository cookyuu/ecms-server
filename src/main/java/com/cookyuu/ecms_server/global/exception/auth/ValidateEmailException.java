package com.cookyuu.ecms_server.global.exception.auth;

import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ValidateEmailException extends ECMSAppException {
    public ValidateEmailException() {
        super(ResultCode.INVALID_INPUT_VALUE);
    }
    public ValidateEmailException(ResultCode resultCode) {
        super(resultCode);
    }
}
