package com.cookyuu.ecms_server.common.exception;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import lombok.Getter;

/**
 * 인증/인가 관련 예외 처리 클래스
 * JWT 토큰 검증, 로그인, 권한 등의 보안 관련 예외 전담
 *
 * 사용 예시:
 * - throw new AuthenticationException(ResultCode.JWT_EXPIRED_TOKEN);
 * - throw new AuthenticationException(ResultCode.ACCESS_DENIED, "관리자 권한 필요");
 */
@Getter
public class AuthenticationException extends RuntimeException {
    private final ResultCode resultCode;

    /**
     * ResultCode만으로 인증 예외 생성
     * @param resultCode JWT, AUTH 관련 에러 코드
     */
    public AuthenticationException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    /**
     * ResultCode + 커스텀 메시지로 인증 예외 생성
     * @param resultCode JWT, AUTH 관련 에러 코드
     * @param customMessage 상세 에러 메시지
     */
    public AuthenticationException(ResultCode resultCode, String customMessage) {
        super(customMessage);
        this.resultCode = resultCode;
    }

    /**
     * 원인 예외를 포함한 인증 예외 생성
     * @param resultCode JWT, AUTH 관련 에러 코드
     * @param cause 원인 예외 (예: JwtException, AuthenticationException 등)
     */
    public AuthenticationException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.resultCode = resultCode;
    }
}
