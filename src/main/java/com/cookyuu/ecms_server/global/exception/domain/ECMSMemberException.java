package com.cookyuu.ecms_server.global.exception.domain;

import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ECMSMemberException extends ECMSAppException {

    public ECMSMemberException() {
        super(ResultCode.MEMBER_NOT_FOUND);
    }

    public ECMSMemberException(ResultCode resultCode) {
        super(resultCode);
    }

    protected ECMSMemberException(ResultCode resultCode, String customMsg) {
        super(resultCode, customMsg);
    }

    protected ECMSMemberException(ResultCode resultCode, Object data, String[] args) {
        super(resultCode, data, args);
    }

    protected ECMSMemberException(ResultCode resultCode, Throwable t) {
        super(resultCode, t);
    }

    protected ECMSMemberException(ResultCode resultCode, Throwable t, String customMsg) {
        super(resultCode, t, customMsg);
    }
}
