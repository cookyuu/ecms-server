package com.cookyuu.ecms_server.global.exception.auth;

import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class UserLoginException extends ECMSAppException {
    public UserLoginException() {
        super(ResultCode.BAD_REQUEST);
    }
    public UserLoginException(ResultCode resultCode) {
        super(resultCode);
    }
}
