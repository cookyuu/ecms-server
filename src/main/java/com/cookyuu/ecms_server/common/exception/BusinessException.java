package com.cookyuu.ecms_server.common.exception;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import lombok.Getter;

/**
 * 비즈니스 로직 예외 처리를 위한 기본 예외 클래스
 * 모든 도메인(주문, 결제, 상품 등)의 예외를 통합 관리
 * ResultCode를 통해 예외 유형을 구분
 */
@Getter
public class BusinessException extends RuntimeException {
    private final ResultCode resultCode;
    private final Object data;

    /**
     * ResultCode만으로 예외 생성 (가장 일반적인 케이스)
     * @param resultCode 에러 코드 정보
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
        this.data = null;
    }

    /**
     * ResultCode + 커스텀 메시지로 예외 생성
     * @param resultCode 에러 코드 정보
     * @param customMessage 상세 에러 메시지 (ResultCode 메시지 오버라이드)
     */
    public BusinessException(ResultCode resultCode, String customMessage) {
        super(customMessage);
        this.resultCode = resultCode;
        this.data = null;
    }

    /**
     * ResultCode + 추가 데이터로 예외 생성
     * @param resultCode 에러 코드 정보
     * @param customMessage 상세 에러 메시지
     * @param data 클라이언트에 전달할 추가 데이터
     */
    public BusinessException(ResultCode resultCode, String customMessage, Object data) {
        super(customMessage);
        this.resultCode = resultCode;
        this.data = data;
    }

    /**
     * 원인 예외를 포함한 예외 생성 (외부 라이브러리 예외 래핑용)
     * @param resultCode 에러 코드 정보
     * @param cause 원인 예외
     */
    public BusinessException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.resultCode = resultCode;
        this.data = null;
    }

    /**
     * 원인 예외 + 커스텀 메시지로 예외 생성
     * @param resultCode 에러 코드 정보
     * @param customMessage 상세 에러 메시지
     * @param cause 원인 예외
     */
    public BusinessException(ResultCode resultCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.resultCode = resultCode;
        this.data = null;
    }
}
