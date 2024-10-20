package com.cookyuu.ecms_server.global.exception.domain;

import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ECMSOrderException extends ECMSAppException {

    public ECMSOrderException() {
        super(ResultCode.ORDER_NOT_FOUND);
    }

    public ECMSOrderException(ResultCode resultCode) {
        super(resultCode);
    }

    public ECMSOrderException(ResultCode resultCode, String customMsg) {
        super(resultCode, customMsg);
    }

    protected ECMSOrderException(ResultCode resultCode, Object data, String[] args) {
        super(resultCode, data, args);
    }

    protected ECMSOrderException(ResultCode resultCode, Throwable t) {
        super(resultCode, t);
    }

    protected ECMSOrderException(ResultCode resultCode, Throwable t, String customMsg) {
        super(resultCode, t, customMsg);
    }
}
