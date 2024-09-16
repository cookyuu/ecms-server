package com.cookyuu.ecms_server.global.exception.domain;

import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ECMSCartItemException extends ECMSAppException {

    public ECMSCartItemException() {
        super(ResultCode.CARTITEM_NOT_FOUND);
    }

    public ECMSCartItemException(ResultCode resultCode) {
        super(resultCode);
    }

    public ECMSCartItemException(ResultCode resultCode, String customMsg) {
        super(resultCode, customMsg);
    }

    protected ECMSCartItemException(ResultCode resultCode, Object data, String[] args) {
        super(resultCode, data, args);
    }

    protected ECMSCartItemException(ResultCode resultCode, Throwable t) {
        super(resultCode, t);
    }

    protected ECMSCartItemException(ResultCode resultCode, Throwable t, String customMsg) {
        super(resultCode, t, customMsg);
    }
}
