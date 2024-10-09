package com.cookyuu.ecms_server.global.exception.domain;

import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ECMSPaymentException extends ECMSAppException {

    public ECMSPaymentException() {
        super(ResultCode.PAYMENT_NOT_FOUND);
    }

    public ECMSPaymentException(ResultCode resultCode) {
        super(resultCode);
    }

    public ECMSPaymentException(ResultCode resultCode, String customMsg) {
        super(resultCode, customMsg);
    }

    protected ECMSPaymentException(ResultCode resultCode, Object data, String[] args) {
        super(resultCode, data, args);
    }

    protected ECMSPaymentException(ResultCode resultCode, Throwable t) {
        super(resultCode, t);
    }

    protected ECMSPaymentException(ResultCode resultCode, Throwable t, String customMsg) {
        super(resultCode, t, customMsg);
    }
}
