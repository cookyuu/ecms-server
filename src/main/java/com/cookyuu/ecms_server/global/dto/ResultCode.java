package com.cookyuu.ecms_server.global.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST,"C-001", "잘못된 요청값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C-002", "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"C-004", "서버 에러 입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST,"C-005", "잘못된 유형의 값입니다."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C-006", "접근 권한이 없습니다."),
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND,"C-007", "데이터를 찾을 수 없습니다."),



    // Member
    EMAIL_DUPLICATE(HttpStatus.BAD_REQUEST, "M-001", "중복된 이메일 주소입니다."),
    NICKNAME_DUPLICATE(HttpStatus.BAD_REQUEST,"M-002", "중복된 닉네임입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, final String code, final String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
