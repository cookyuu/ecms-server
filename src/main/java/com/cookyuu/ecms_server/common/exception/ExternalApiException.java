package com.cookyuu.ecms_server.common.exception;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import lombok.Getter;

/**
 * 외부 API 호출 실패 예외 처리 클래스
 * Slack, 결제 PG, 배송 조회 등 외부 시스템 연동 실패 시 사용
 *
 * 특징:
 * - 재시도 가능 여부 플래그 제공
 * - 외부 API 이름 및 에러 코드 추적
 * - Circuit Breaker, Retry 로직과 연동 가능
 *
 * 사용 예시:
 * - throw new ExternalApiException("Slack", ResultCode.FAIL_ALERT_SLACK, true);
 * - throw new ExternalApiException("TossPayments", ResultCode.INTERNAL_SERVER_ERROR, "PG-500", false);
 */
@Getter
public class ExternalApiException extends RuntimeException {
    private final String apiName;              // 외부 API 이름 (예: "Slack", "TossPayments")
    private final ResultCode resultCode;       // 내부 에러 코드
    private final String externalErrorCode;    // 외부 API의 에러 코드 (있는 경우)
    private final boolean retryable;           // 재시도 가능 여부

    /**
     * 외부 API 예외 생성 (기본)
     * @param apiName 외부 API 이름
     * @param resultCode 에러 코드
     * @param retryable 재시도 가능 여부
     */
    public ExternalApiException(String apiName, ResultCode resultCode, boolean retryable) {
        super(String.format("[%s] %s", apiName, resultCode.getMessage()));
        this.apiName = apiName;
        this.resultCode = resultCode;
        this.externalErrorCode = null;
        this.retryable = retryable;
    }

    /**
     * 외부 API 예외 생성 (외부 에러 코드 포함)
     * @param apiName 외부 API 이름
     * @param resultCode 에러 코드
     * @param externalErrorCode 외부 API가 반환한 에러 코드
     * @param retryable 재시도 가능 여부
     */
    public ExternalApiException(String apiName, ResultCode resultCode, String externalErrorCode, boolean retryable) {
        super(String.format("[%s] %s (External Error: %s)", apiName, resultCode.getMessage(), externalErrorCode));
        this.apiName = apiName;
        this.resultCode = resultCode;
        this.externalErrorCode = externalErrorCode;
        this.retryable = retryable;
    }

    /**
     * 외부 API 예외 생성 (원인 예외 포함)
     * @param apiName 외부 API 이름
     * @param resultCode 에러 코드
     * @param cause 원인 예외
     * @param retryable 재시도 가능 여부
     */
    public ExternalApiException(String apiName, ResultCode resultCode, Throwable cause, boolean retryable) {
        super(String.format("[%s] %s", apiName, resultCode.getMessage()), cause);
        this.apiName = apiName;
        this.resultCode = resultCode;
        this.externalErrorCode = null;
        this.retryable = retryable;
    }

    /**
     * 외부 API 예외 생성 (모든 정보 포함)
     * @param apiName 외부 API 이름
     * @param resultCode 에러 코드
     * @param externalErrorCode 외부 API의 에러 코드
     * @param cause 원인 예외
     * @param retryable 재시도 가능 여부
     */
    public ExternalApiException(String apiName, ResultCode resultCode, String externalErrorCode, Throwable cause, boolean retryable) {
        super(String.format("[%s] %s (External Error: %s)", apiName, resultCode.getMessage(), externalErrorCode), cause);
        this.apiName = apiName;
        this.resultCode = resultCode;
        this.externalErrorCode = externalErrorCode;
        this.retryable = retryable;
    }
}
