package com.cookyuu.ecms_server.global.exception.domain;

import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.ECMSAppException;

public class ECMSCategoryException extends ECMSAppException {

    public ECMSCategoryException() {
        super(ResultCode.CATEGORY_NOT_FOUND);
    }

    public ECMSCategoryException(ResultCode resultCode) {
        super(resultCode);
    }

    protected ECMSCategoryException(ResultCode resultCode, String customMsg) {
        super(resultCode, customMsg);
    }

    protected ECMSCategoryException(ResultCode resultCode, Object data, String[] args) {
        super(resultCode, data, args);
    }

    protected ECMSCategoryException(ResultCode resultCode, Throwable t) {
        super(resultCode, t);
    }

    protected ECMSCategoryException(ResultCode resultCode, Throwable t, String customMsg) {
        super(resultCode, t, customMsg);
    }
}
