package com.cookyuu.ecms_server.global.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResultCode {

    // Common - SUCCESS
    SUCCESS(HttpStatus.OK,"0000","성공!"),
    CREATED(HttpStatus.CREATED,"0000", "성공!"),


    // Common - ERROR
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST,"C-001", "잘못된 요청값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C-002", "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"C-004", "서버 에러 입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST,"C-005", "잘못된 유형의 값입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C-006", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND,"C-007", "데이터를 찾을 수 없습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "C-008", "잘못된 요청입니다."),


    // Member - SUCCESS
    VALID_USERID_SUCCESS(HttpStatus.OK, "0000", "유저 아이디 유효성 검증 완료. 사용 가능한 유저 아이디입니다."),

    // Member - ERROR
    VALID_EMAIL_DUPLICATE(HttpStatus.BAD_REQUEST, "M-001", "이메일 유효성 검증 실패. 이미 등록된 이메일입니다."),
    VALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "M-002", "이메일 유효성 검증 실패. 잘못된 이메일 형식입니다."),
    VALID_PHONENUMBER_DUPLICATE(HttpStatus.BAD_REQUEST,"M-003", "핸드폰 번호 유효성 검증 실패. 이미 등록된 핸드폰 번호입니다."),
    VALID_PHONENUMBER_FORMAT(HttpStatus.BAD_REQUEST, "M-004", "핸드폰 번호 유효성 검증 실패. 잘못된 형식의 핸드폰 번호입니다."),
    VALID_USERID_DUPLICATE(HttpStatus.BAD_REQUEST,"M-005", "유저 아이디 유효성 검증 실패. 이미 등록된 아이디입니다."),
    VALID_USERID_FORMAT(HttpStatus.BAD_REQUEST, "M-006", "유저 아이디 유효성 검증 실패. 잘못된 아이디 형식입니다."),
    VALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "M-007", "패스워드 유효성 검증 실패. 잘못된 패스워드 형식입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ResultCode (HttpStatus status, final String code, final String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
