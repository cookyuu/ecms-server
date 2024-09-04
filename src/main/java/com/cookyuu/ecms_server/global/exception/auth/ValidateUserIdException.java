package com.cookyuu.ecms_server.global.exception.auth;

import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ValidateUserIdException extends ECMSAppException {
    public ValidateUserIdException() {
        super(ResultCode.INVALID_INPUT_VALUE);
    }
    public ValidateUserIdException(ResultCode resultCode) {
        super(resultCode);
    }
}
