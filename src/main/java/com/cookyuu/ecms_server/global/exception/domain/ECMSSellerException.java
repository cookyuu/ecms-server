package com.cookyuu.ecms_server.global.exception.domain;

import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ECMSSellerException extends ECMSAppException {

    public ECMSSellerException() {
        super(ResultCode.SELLER_NOT_FOUND);
    }

    public ECMSSellerException(ResultCode resultCode) {
        super(resultCode);
    }

    protected ECMSSellerException(ResultCode resultCode, String customMsg) {
        super(resultCode, customMsg);
    }

    protected ECMSSellerException(ResultCode resultCode, Object data, String[] args) {
        super(resultCode, data, args);
    }

    protected ECMSSellerException(ResultCode resultCode, Throwable t) {
        super(resultCode, t);
    }

    protected ECMSSellerException(ResultCode resultCode, Throwable t, String customMsg) {
        super(resultCode, t, customMsg);
    }
}
