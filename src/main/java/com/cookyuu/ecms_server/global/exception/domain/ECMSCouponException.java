package com.cookyuu.ecms_server.global.exception.domain;

import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ECMSCouponException extends ECMSAppException {

    public ECMSCouponException() {
        super(ResultCode.COUPON_NOT_FOUND);
    }

    public ECMSCouponException(ResultCode resultCode) {
        super(resultCode);
    }

    protected ECMSCouponException(ResultCode resultCode, String customMsg) {
        super(resultCode, customMsg);
    }

    protected ECMSCouponException(ResultCode resultCode, Object data, String[] args) {
        super(resultCode, data, args);
    }

    public ECMSCouponException(ResultCode resultCode, Throwable t) {
        super(resultCode, t);
    }

    protected ECMSCouponException(ResultCode resultCode, Throwable t, String customMsg) {
        super(resultCode, t, customMsg);
    }
}
