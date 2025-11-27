package com.cookyuu.ecms_server.common.web.advice;

import com.cookyuu.ecms_server.common.web.dto.ApiResponse;
import com.cookyuu.ecms_server.common.web.filter.RequestContextFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Slf4j
@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {

        HttpServletRequest servletRequest = extractHttpServletRequest(request);
        if (servletRequest == null) {
            log.warn("[ApiResponseAdvice] HttpServletRequest를 추출할 수 없습니다.");
            return body;
        }

        String path = extractRequestPath(servletRequest);
        String traceId = extractTraceId(servletRequest);

        ApiResponse<?> apiResponse = extractApiResponse(body);
        if (apiResponse == null) {
            return body;
        }

        ApiResponse<?> enrichedResponse = apiResponse.withContext(path, traceId);

        if (body instanceof ResponseEntity<?> responseEntity) {
            return ResponseEntity
                    .status(responseEntity.getStatusCode())
                    .headers(responseEntity.getHeaders())
                    .body(enrichedResponse);
        }
        return enrichedResponse;
    }

    /**
     * body에서 ApiResponse 추출
     * ResponseEntity<ApiResponse> 또는 ApiResponse 모두 처리
     */
    private ApiResponse<?> extractApiResponse(Object body) {
        if (body instanceof ResponseEntity<?> responseEntity) {
            Object responseBody = responseEntity.getBody();
            if (responseBody instanceof ApiResponse<?> apiResponse) {
                return apiResponse;
            }
        } else if (body instanceof ApiResponse<?> apiResponse) {
            return apiResponse;
        }
        return null;
    }

    /**
     * ServerHttpRequest에서 HttpServletRequest 추출
     */
    private HttpServletRequest extractHttpServletRequest(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            return servletRequest.getServletRequest();
        }
        return null;
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
}
