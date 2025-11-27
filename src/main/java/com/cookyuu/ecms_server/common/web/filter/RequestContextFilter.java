package com.cookyuu.ecms_server.common.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 요청 컨텍스트 필터
 *
 * 모든 HTTP 요청에 대해:
 * 1. 고유한 traceId 생성 (요청 추적용)
 * 2. MDC에 traceId 저장 (로깅에서 활용)
 * 3. Request Attribute에 저장 (응답 생성 시 활용)
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestContextFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";
    public static final String TRACE_ID_ATTRIBUTE = "traceId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. traceId 생성 또는 헤더에서 추출
        String traceId = extractOrGenerateTraceId(request);

        try {
            // 2. MDC에 저장 (로깅용)
            MDC.put(TRACE_ID_MDC_KEY, traceId);

            // 3. Request Attribute에 저장 (응답 생성용)
            request.setAttribute(TRACE_ID_ATTRIBUTE, traceId);

            // 4. Response Header에 traceId 추가
            response.setHeader(TRACE_ID_HEADER, traceId);

            // 5. 다음 필터 체인 실행
            filterChain.doFilter(request, response);

        } finally {
            // 6. MDC 정리 (메모리 누수 방지)
            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }

    /**
     * traceId 추출 또는 생성
     * 1. 요청 헤더에서 추출 시도
     * 2. 없으면 새로 생성
     */
    private String extractOrGenerateTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_ID_HEADER);

        if (traceId == null || traceId.isBlank()) {
            traceId = generateTraceId();
        }

        return traceId;
    }

    /**
     * UUID 기반 traceId 생성
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
