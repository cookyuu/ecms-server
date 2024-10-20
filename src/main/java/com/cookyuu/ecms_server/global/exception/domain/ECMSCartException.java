package com.cookyuu.ecms_server.global.exception.domain;

import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ECMSCartException extends ECMSAppException {

    public ECMSCartException() {
        super(ResultCode.CART_NOT_FOUND);
    }

    public ECMSCartException(ResultCode resultCode) {
        super(resultCode);
    }

    protected ECMSCartException(ResultCode resultCode, String customMsg) {
        super(resultCode, customMsg);
    }

    protected ECMSCartException(ResultCode resultCode, Object data, String[] args) {
        super(resultCode, data, args);
    }

    protected ECMSCartException(ResultCode resultCode, Throwable t) {
        super(resultCode, t);
    }

    protected ECMSCartException(ResultCode resultCode, Throwable t, String customMsg) {
        super(resultCode, t, customMsg);
    }
}
