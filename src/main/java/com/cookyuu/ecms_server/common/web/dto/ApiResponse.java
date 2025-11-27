package com.cookyuu.ecms_server.common.web.dto;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {


    private final Boolean success;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;
    private final String path;
    private final String traceId;
    private final String code;
    private final String message;
    private final Integer status;
    private final T data;
    private final PaginationInfo pagination;
    private final List<ErrorDetail> errors;
    private final Map<String, Object> metadata;
    @JsonIgnore
    private final ResultCode resultCode;

    @Builder(builderMethodName = "internalBuilder")
    private ApiResponse(
            Boolean success,
            LocalDateTime timestamp,
            String path,
            String traceId,
            String code,
            String message,
            Integer status,
            T data,
            PaginationInfo pagination,
            List<ErrorDetail> errors,
            Map<String, Object> metadata,
            ResultCode resultCode
    ) {
        this.success = success;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.path = path;
        this.traceId = traceId;
        this.code = code;
        this.message = message;
        this.status = status;
        this.data = data;
        this.pagination = pagination;
        this.errors = errors;
        this.metadata = metadata;
        this.resultCode = resultCode;
    }

    // ========== Success Response Factory Methods ==========

    /**
     * 성공 응답 (데이터 없음)
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>internalBuilder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .status(HttpStatus.OK.value())
                .resultCode(ResultCode.SUCCESS)
                .build();
    }

    /**
     * 성공 응답 (커스텀 ResultCode)
     */
    public static <T> ApiResponse<T> success(ResultCode resultCode) {
        return ApiResponse.<T>internalBuilder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .status(resultCode.getStatus().value())
                .resultCode(resultCode)
                .build();
    }

    /**
     * 성공 응답 (데이터 포함)
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>internalBuilder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .status(HttpStatus.OK.value())
                .data(data)
                .resultCode(ResultCode.SUCCESS)
                .build();
    }

    /**
     * 성공 응답 (ResultCode + 데이터)
     */
    public static <T> ApiResponse<T> success(ResultCode resultCode, T data) {
        return ApiResponse.<T>internalBuilder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .status(resultCode.getStatus().value())
                .data(data)
                .resultCode(resultCode)
                .build();
    }

    /**
     * 성공 응답 (List)
     */
    public static <T> ApiResponse<List<T>> success(List<T> data) {
        return ApiResponse.<List<T>>internalBuilder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .status(HttpStatus.OK.value())
                .data(data)
                .resultCode(ResultCode.SUCCESS)
                .build();
    }

    /**
     * 성공 응답 (Page - 페이징 정보 포함)
     */
    public static <T> ApiResponse<List<T>> success(Page<T> page) {
        return ApiResponse.<List<T>>internalBuilder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .status(HttpStatus.OK.value())
                .data(page.getContent())
                .pagination(PaginationInfo.from(page))
                .resultCode(ResultCode.SUCCESS)
                .build();
    }

    // ========== Created Response Factory Methods ==========

    /**
     * 리소스 생성 성공 응답
     */
    public static <T> ApiResponse<T> created() {
        return ApiResponse.<T>internalBuilder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .code(ResultCode.CREATED.getCode())
                .message(ResultCode.CREATED.getMessage())
                .status(HttpStatus.CREATED.value())
                .resultCode(ResultCode.CREATED)
                .build();
    }

    /**
     * 리소스 생성 성공 (데이터 포함)
     */
    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>internalBuilder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .code(ResultCode.CREATED.getCode())
                .message(ResultCode.CREATED.getMessage())
                .status(HttpStatus.CREATED.value())
                .data(data)
                .resultCode(ResultCode.CREATED)
                .build();
    }

    /**
     * 리소스 생성 성공 (커스텀 ResultCode)
     */
    public static <T> ApiResponse<T> created(ResultCode resultCode, T data) {
        return ApiResponse.<T>internalBuilder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .status(HttpStatus.CREATED.value())
                .data(data)
                .resultCode(resultCode)
                .build();
    }

    // ========== Failure Response Factory Methods ==========

    /**
     * 실패 응답
     */
    public static <T> ApiResponse<T> failure(ResultCode resultCode) {
        return ApiResponse.<T>internalBuilder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .status(resultCode.getStatus().value())
                .resultCode(resultCode)
                .build();
    }

    /**
     * 실패 응답 (커스텀 메시지)
     */
    public static <T> ApiResponse<T> failure(ResultCode resultCode, String customMessage) {
        return ApiResponse.<T>internalBuilder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .code(resultCode.getCode())
                .message(customMessage)
                .status(resultCode.getStatus().value())
                .resultCode(resultCode)
                .build();
    }

    /**
     * 실패 응답 (에러 상세 포함)
     */
    public static <T> ApiResponse<T> failure(ResultCode resultCode, String customMessage, List<ErrorDetail> errors) {
        return ApiResponse.<T>internalBuilder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .code(resultCode.getCode())
                .message(customMessage)
                .status(resultCode.getStatus().value())
                .errors(errors)
                .resultCode(resultCode)
                .build();
    }

    /**
     * 실패 응답 (디버그 데이터 포함 - 개발 환경용)
     */
    public static ApiResponse<Object> failure(ResultCode resultCode, String customMessage, Object debugData) {
        return ApiResponse.internalBuilder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .code(resultCode.getCode())
                .message(customMessage)
                .status(resultCode.getStatus().value())
                .data(debugData)
                .resultCode(resultCode)
                .build();
    }

    // ========== Builder Methods for Advanced Usage ==========

    /**
     * 컨텍스트 정보 추가 (path, traceId)
     */
    public ApiResponse<T> withContext(String path, String traceId) {
        return ApiResponse.<T>internalBuilder()
                .success(this.success)
                .timestamp(this.timestamp)
                .path(path)
                .traceId(traceId)
                .code(this.code)
                .message(this.message)
                .status(this.status)
                .data(this.data)
                .pagination(this.pagination)
                .errors(this.errors)
                .metadata(this.metadata)
                .resultCode(this.resultCode)
                .build();
    }

    /**
     * 메타데이터 추가
     */
    public ApiResponse<T> withMetadata(String key, Object value) {
        Map<String, Object> newMetadata = this.metadata != null
                ? new HashMap<>(this.metadata)
                : new HashMap<>();
        newMetadata.put(key, value);

        return ApiResponse.<T>internalBuilder()
                .success(this.success)
                .timestamp(this.timestamp)
                .path(this.path)
                .traceId(this.traceId)
                .code(this.code)
                .message(this.message)
                .status(this.status)
                .data(this.data)
                .pagination(this.pagination)
                .errors(this.errors)
                .metadata(newMetadata)
                .resultCode(this.resultCode)
                .build();
    }

    // ========== Inner Classes ==========

    /**
     * 페이징 정보
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaginationInfo {
        /**
         * 현재 페이지 번호 (0부터 시작)
         */
        private final Integer page;

        /**
         * 페이지당 항목 수
         */
        private final Integer size;

        /**
         * 전체 항목 수
         */
        private final Long totalElements;

        /**
         * 전체 페이지 수
         */
        private final Integer totalPages;

        /**
         * 첫 페이지 여부
         */
        private final Boolean first;

        /**
         * 마지막 페이지 여부
         */
        private final Boolean last;

        /**
         * 다음 페이지 존재 여부
         */
        private final Boolean hasNext;

        /**
         * 이전 페이지 존재 여부
         */
        private final Boolean hasPrevious;

        public static PaginationInfo from(Page<?> page) {
            return PaginationInfo.builder()
                    .page(page.getNumber())
                    .size(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .first(page.isFirst())
                    .last(page.isLast())
                    .hasNext(page.hasNext())
                    .hasPrevious(page.hasPrevious())
                    .build();
        }
    }

    /**
     * 에러 상세 정보 (Validation 실패 등)
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetail {
        /**
         * 에러가 발생한 필드명 (validation error)
         */
        private final String field;

        /**
         * 거부된 값
         */
        private final Object rejectedValue;

        /**
         * 에러 코드
         */
        private final String code;

        /**
         * 에러 메시지
         */
        private final String message;

        public static ErrorDetail of(String field, Object rejectedValue, String code, String message) {
            return ErrorDetail.builder()
                    .field(field)
                    .rejectedValue(rejectedValue)
                    .code(code)
                    .message(message)
                    .build();
        }

        public static ErrorDetail of(String field, String message) {
            return ErrorDetail.builder()
                    .field(field)
                    .message(message)
                    .build();
        }
    }
}
