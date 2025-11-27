package com.cookyuu.ecms_server.common.exception;

//import com.cookyuu.ecms_server.domain.alert.service.SlackService;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.web.dto.ApiResponse;
import com.cookyuu.ecms_server.common.web.filter.RequestContextFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

/**
 * 전역 예외 처리 핸들러
 *
 * 예외 처리 우선순위:
 * 1. 커스텀 예외 (BusinessException, AuthenticationException, ExternalApiException)
 * 2. Spring Security 예외 (BadCredentialsException, AccessDeniedException)
 * 3. Validation 예외 (MethodArgumentNotValidException)
 * 4. 표준 Java 예외 (IllegalArgumentException, NullPointerException 등)
 * 5. 최후의 Exception (모든 예외)
 */
@Primary
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
//    private final SlackService slackService;

    // ========== 헬퍼 메서드 ==========

    /**
     * ApiResponse에 컨텍스트 정보(path, traceId) 추가
     */
    private <T> ApiResponse<T> addContext(ApiResponse<T> response, HttpServletRequest request) {
        String path = extractRequestPath(request);
        String traceId = extractTraceId(request);
        return response.withContext(path, traceId);
    }

    /**
     * 요청 경로 추출 (쿼리 파라미터 포함)
     */
    private String extractRequestPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        if (queryString != null && !queryString.isBlank()) {
            return uri + "?" + queryString;
        }

        return uri;
    }

    /**
     * Request Attribute에서 traceId 추출
     */
    private String extractTraceId(HttpServletRequest request) {
        Object traceId = request.getAttribute(RequestContextFilter.TRACE_ID_ATTRIBUTE);
        return traceId != null ? traceId.toString() : null;
    }

    // ========== 커스텀 예외 처리 ==========

    /**
     * 비즈니스 로직 예외 처리
     * 주문, 결제, 상품 등 모든 도메인의 비즈니스 예외를 처리
     */
    @ExceptionHandler(value = BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(HttpServletRequest request, BusinessException e) {
        String errMsg = e.getMessage() != null ? e.getMessage() : e.getResultCode().getMessage();
        log.warn("[BusinessException] code={}, message={}", e.getResultCode().getCode(), errMsg);

        var response = ApiResponse.failure(e.getResultCode(), errMsg);
        if (e.getData() != null) {
            response = ApiResponse.failure(e.getResultCode(), errMsg, e.getData());
        }
        response = addContext(response, request);

        return new ResponseEntity<>(response, e.getResultCode().getStatus());
    }

    /**
     * 인증/인가 예외 처리
     * JWT 토큰, 로그인, 권한 검증 실패 시 처리
     */
    @ExceptionHandler(value = AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(HttpServletRequest request, AuthenticationException e) {
        String errMsg = e.getMessage() != null ? e.getMessage() : e.getResultCode().getMessage();
        log.warn("[AuthenticationException] code={}, message={}", e.getResultCode().getCode(), errMsg);

        var response = ApiResponse.failure(e.getResultCode(), errMsg);
        response = addContext(response, request);
        return new ResponseEntity<>(response, e.getResultCode().getStatus());
    }

    /**
     * 외부 API 호출 실패 예외 처리
     * Slack, PG사, 배송 조회 등 외부 시스템 연동 실패 시 처리
     */
    @ExceptionHandler(value = ExternalApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleExternalApiException(HttpServletRequest request, ExternalApiException e) {
        log.error("[ExternalApiException] api={}, code={}, externalCode={}, retryable={}, message={}",
                  e.getApiName(),
                  e.getResultCode().getCode(),
                  e.getExternalErrorCode(),
                  e.isRetryable(),
                  e.getMessage(),
                  e);

        // 외부 API 실패는 클라이언트에게 간결한 메시지만 전달
        var response = ApiResponse.failure(
            e.getResultCode(),
            "외부 서비스 연동 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요."
        );
        response = addContext(response, request);
        return new ResponseEntity<>(response, e.getResultCode().getStatus());
    }

    // ========== Spring Security 예외 처리 ==========

    @ExceptionHandler(value = BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(HttpServletRequest request, BadCredentialsException e) {
        log.error("[BadCredentialsException] ", e);
        var response = ApiResponse.failure(ResultCode.BAD_CREDENTIAL, e.getMessage());
        response = addContext(response, request);
        return new ResponseEntity<>(response, ResultCode.BAD_CREDENTIAL.getStatus());
    }
    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(HttpServletRequest request, AccessDeniedException e) {
        log.error("[AccessDeniedException] ", e);
        var response = ApiResponse.failure(ResultCode.ACCESS_DENIED, e.getMessage());
        response = addContext(response, request);
        return new ResponseEntity<>(response, ResultCode.ACCESS_DENIED.getStatus());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e) {
        log.error("[MethodArgumentNotValidException] ", e);
        var response = ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage());
        response = addContext(response, request);
        return new ResponseEntity<>(response, ResultCode.BAD_REQUEST.getStatus());
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoSuchElementException(HttpServletRequest request, HttpRequestMethodNotSupportedException e) {
        log.error("[HttpRequestMethodNotSupportedException] ", e);
        var response = ApiResponse.failure(ResultCode.METHOD_NOT_ALLOWED, e.getMessage());
        response = addContext(response, request);
        return new ResponseEntity<>(response, ResultCode.METHOD_NOT_ALLOWED.getStatus());
    }

    @ExceptionHandler(value = NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoSuchElementException(HttpServletRequest request, NoSuchElementException e) {
        log.error("[NoSuchElementException] ", e);
        var response = ApiResponse.failure(ResultCode.NOT_FOUND, e.getMessage());
        response = addContext(response, request);
        return new ResponseEntity<>(response, ResultCode.NOT_FOUND.getStatus());
    }

    @ExceptionHandler(value = SQLException.class)
    public ResponseEntity<ApiResponse<Object>> handleSQLException(HttpServletRequest request, SQLException e) {
        log.error("[SQLException] ", e);
        var response = ApiResponse.failure(ResultCode.INTERNAL_SERVER_ERROR, e.getMessage());
        response = addContext(response, request);
        return new ResponseEntity<>(response, ResultCode.INTERNAL_SERVER_ERROR.getStatus());
    }

    @ExceptionHandler(value = IOException.class)
    public ResponseEntity<ApiResponse<Object>> handleIOException(HttpServletRequest request, IOException e) {
        log.error("[IOException] ", e);
        var response = ApiResponse.failure(ResultCode.INTERNAL_SERVER_ERROR, e.getMessage());
        response = addContext(response, request);
        return new ResponseEntity<>(response, ResultCode.INTERNAL_SERVER_ERROR.getStatus());
    }

    @ExceptionHandler(value = IndexOutOfBoundsException.class)
    public ResponseEntity<ApiResponse<Object>> handleIndexOutOfBoundsException(HttpServletRequest request, IndexOutOfBoundsException e) {
        log.error("[IndexOutOfBoundsException] ", e);
        var response = ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage());
        response = addContext(response, request);
        return new ResponseEntity<>(response, ResultCode.BAD_REQUEST.getStatus());
    }

    @ExceptionHandler(value = IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(HttpServletRequest request, IllegalStateException e) {
        log.error("[IllegalArgumentException] ", e);
        var response = ApiResponse.failure(ResultCode.INTERNAL_SERVER_ERROR, e.getMessage());
        response = addContext(response, request);
        return new ResponseEntity<>(response, ResultCode.INTERNAL_SERVER_ERROR.getStatus());
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(HttpServletRequest request, IllegalArgumentException e) {
        log.error("[IllegalArgumentException] ", e);
        var response = ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage());
        response = addContext(response, request);
        return new ResponseEntity<>(response, ResultCode.BAD_REQUEST.getStatus());
    }

    @ExceptionHandler(value = HttpMessageConversionException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageConversionException(HttpServletRequest request, HttpMessageConversionException e) {
        log.error("[HttpMessageConversionException] ", e);
        var response = ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage());
        response = addContext(response, request);
        return new ResponseEntity<>(response, ResultCode.BAD_REQUEST.getStatus());
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(HttpServletRequest request, ConstraintViolationException e) {
        log.error("[ConstraintViolationException] ", e);
        var response = ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage());
        response = addContext(response, request);
        return new ResponseEntity<>(response, ResultCode.BAD_REQUEST.getStatus());
    }


    @ExceptionHandler(value = DateTimeParseException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintDateTimeParseException(HttpServletRequest request, DateTimeParseException e) {
        log.error("[DateTimeParseException] ", e);
        var response = ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage());
        response = addContext(response, request);
        return new ResponseEntity<>(response, ResultCode.BAD_REQUEST.getStatus());
    }

    @ExceptionHandler(value = NullPointerException.class)
    public ResponseEntity<ApiResponse<Object>> handleNullPointerException(HttpServletRequest request, NullPointerException e) {
     log.error("[NullPointerException] ", e);
     var response = ApiResponse.failure(ResultCode.INTERNAL_SERVER_ERROR, e.getMessage());
     response = addContext(response, request);
     return new ResponseEntity<>(response, ResultCode.INTERNAL_SERVER_ERROR.getStatus());
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(HttpServletRequest request, Exception e) {
        log.error("[Exception] ", e);
//        slackService.sendErrorForSlack(e);
        var response = ApiResponse.failure(ResultCode.INTERNAL_SERVER_ERROR, e.getMessage());
        response = addContext(response, request);
        return new ResponseEntity<>(response, ResultCode.INTERNAL_SERVER_ERROR.getStatus());
    }
}
