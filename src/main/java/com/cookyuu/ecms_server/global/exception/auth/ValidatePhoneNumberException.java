package com.cookyuu.ecms_server.global.exception.auth;

import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ValidatePhoneNumberException extends ECMSAppException {
    public ValidatePhoneNumberException() {
        super(ResultCode.INVALID_INPUT_VALUE);
    }
    public ValidatePhoneNumberException(ResultCode resultCode) {
        super(resultCode);
    }
}
