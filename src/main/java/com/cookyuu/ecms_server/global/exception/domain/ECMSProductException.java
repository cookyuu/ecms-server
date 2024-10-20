package com.cookyuu.ecms_server.global.exception.domain;

import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ECMSProductException extends ECMSAppException {

    public ECMSProductException() {
        super(ResultCode.PRODUCT_NOT_FOUND);
    }

    public ECMSProductException(ResultCode resultCode) {
        super(resultCode);
    }

    public ECMSProductException(ResultCode resultCode, String customMsg) {
        super(resultCode, customMsg);
    }

    protected ECMSProductException(ResultCode resultCode, Object data, String[] args) {
        super(resultCode, data, args);
    }

    protected ECMSProductException(ResultCode resultCode, Throwable t) {
        super(resultCode, t);
    }

    protected ECMSProductException(ResultCode resultCode, Throwable t, String customMsg) {
        super(resultCode, t, customMsg);
    }
}
