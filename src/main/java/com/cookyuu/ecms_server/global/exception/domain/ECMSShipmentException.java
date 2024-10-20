package com.cookyuu.ecms_server.global.exception.domain;

import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ECMSShipmentException extends ECMSAppException {

    public ECMSShipmentException() {
        super(ResultCode.SELLER_NOT_FOUND);
    }

    public ECMSShipmentException(ResultCode resultCode) {
        super(resultCode);
    }

    protected ECMSShipmentException(ResultCode resultCode, String customMsg) {
        super(resultCode, customMsg);
    }

    protected ECMSShipmentException(ResultCode resultCode, Object data, String[] args) {
        super(resultCode, data, args);
    }

    public ECMSShipmentException(ResultCode resultCode, Throwable t) {
        super(resultCode, t);
    }

    protected ECMSShipmentException(ResultCode resultCode, Throwable t, String customMsg) {
        super(resultCode, t, customMsg);
    }
}
