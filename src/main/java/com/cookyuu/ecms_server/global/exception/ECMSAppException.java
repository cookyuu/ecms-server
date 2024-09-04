package com.cookyuu.ecms_server.global.exception;

import com.cookyuu.ecms_server.global.dto.ResultCode;
import lombok.Getter;

@Getter
public abstract class ECMSAppException extends RuntimeException{
    private final ResultCode resultCode;
    private String message;
    private String[] args;
    private Object data;

    protected ECMSAppException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    protected ECMSAppException(ResultCode resultCode, String customMsg) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
        this.message = customMsg;
    }

    protected ECMSAppException(ResultCode resultCode, Object data, String[] args) {
        super(resultCode.getMessage());
        this.data = data;
        this.args = args;
        this.resultCode = resultCode;
    }

    protected ECMSAppException(ResultCode resultCode, Throwable t) {
        super(t);
        this.resultCode = resultCode;
    }

    protected ECMSAppException(ResultCode resultCode, Throwable t, String customMsg) {
        super(t);
        this.resultCode = resultCode;
        this.message = customMsg;
    }



}
