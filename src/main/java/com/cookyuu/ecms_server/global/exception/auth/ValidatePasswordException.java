package com.cookyuu.ecms_server.global.exception.auth;

import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ValidatePasswordException extends ECMSAppException {
    public ValidatePasswordException() {
        super(ResultCode.VALID_PASSWORD_FORMAT);
    }
}
