package com.cookyuu.ecms_server.global.exception.auth;

import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class AuthLoginException extends ECMSAppException {
    public AuthLoginException() {
        super(ResultCode.BAD_REQUEST);
    }
    public AuthLoginException(ResultCode resultCode) {
        super(resultCode);
    }
}
