package com.cookyuu.ecms_server.global.exception.domain;

import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ECMSAlertException extends ECMSAppException {

    public ECMSAlertException() {
        super(ResultCode.FAIL_ALERT_SLACK);
    }

    public ECMSAlertException(ResultCode resultCode) {
        super(resultCode);
    }

    protected ECMSAlertException(ResultCode resultCode, String customMsg) {
        super(resultCode, customMsg);
    }

    protected ECMSAlertException(ResultCode resultCode, Object data, String[] args) {
        super(resultCode, data, args);
    }

    public ECMSAlertException(ResultCode resultCode, Throwable t) {
        super(resultCode, t);
    }

    protected ECMSAlertException(ResultCode resultCode, Throwable t, String customMsg) {
        super(resultCode, t, customMsg);
    }
}
