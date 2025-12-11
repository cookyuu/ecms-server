package com.cookyuu.ecms_server.common.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Slf4j
@Component
@Order(1)
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();

        try {
            // MDC 초기화 및 설정
            setupMDC(httpRequest);

            // 요청 로깅
            logRequest(httpRequest);

            // 다음 필터로 전달
            chain.doFilter(request, response);

        } finally {
            // 응답 로깅
            long duration = System.currentTimeMillis() - startTime;
            logResponse(httpRequest, httpResponse, duration);

            // MDC 클리어 (메모리 누수 방지)
            clearMDC();
        }
    }

    private void setupMDC(HttpServletRequest request) {
        // Trace ID: 요청 전체 추적용 (헤더에서 가져오거나 새로 생성)
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }
        MDC.put(TRACE_ID, traceId);

        // Request ID: 개별 요청 식별용
        String requestId = UUID.randomUUID().toString();
        MDC.put(REQUEST_ID, requestId);

        // User ID: 인증된 사용자 ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            try {
                String userId = authentication.getName();
                if (userId != null && !userId.isEmpty()) {
                    MDC.put(USER_ID, userId);
                }
            } catch (Exception e) {
                // 인증 정보 추출 실패 시 무시
            }
        }

        // IP Address
        String ipAddress = getClientIpAddress(request);
        MDC.put(IP_ADDRESS, ipAddress);

        // User Agent
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && !userAgent.isEmpty()) {
            MDC.put(USER_AGENT, userAgent);
        }

        // HTTP Method & Path
        MDC.put(HTTP_METHOD, request.getMethod());
        MDC.put(HTTP_PATH, request.getRequestURI());
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For는 여러 IP를 포함할 수 있음 (첫 번째가 클라이언트 IP)
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    private void logRequest(HttpServletRequest request) {
        // 정적 리소스나 헬스체크는 로그 제외
        String path = request.getRequestURI();
        if (shouldSkipLogging(path)) {
            return;
        }

        log.atDebug()
            .addKeyValue("event", "HTTP_REQUEST")
            .addKeyValue(HTTP_METHOD, request.getMethod())
            .addKeyValue(HTTP_PATH, path)
            .addKeyValue("query_string", request.getQueryString())
            .log("HTTP request received");
    }

    private void logResponse(HttpServletRequest request, HttpServletResponse response, long duration) {
        // 정적 리소스나 헬스체크는 로그 제외
        String path = request.getRequestURI();
        if (shouldSkipLogging(path)) {
            return;
        }

        int statusCode = response.getStatus();
        String logLevel = statusCode >= 500 ? "error" : statusCode >= 400 ? "warn" : "info";

        if ("error".equals(logLevel)) {
            log.atError()
                .addKeyValue("event", "HTTP_RESPONSE")
                .addKeyValue(HTTP_METHOD, request.getMethod())
                .addKeyValue(HTTP_PATH, path)
                .addKeyValue(HTTP_STATUS, statusCode)
                .addKeyValue(DURATION_MS, duration)
                .log("HTTP request completed with error");
        } else if ("warn".equals(logLevel)) {
            log.atWarn()
                .addKeyValue("event", "HTTP_RESPONSE")
                .addKeyValue(HTTP_METHOD, request.getMethod())
                .addKeyValue(HTTP_PATH, path)
                .addKeyValue(HTTP_STATUS, statusCode)
                .addKeyValue(DURATION_MS, duration)
                .log("HTTP request completed with client error");
        } else {
            log.atInfo()
                .addKeyValue("event", "HTTP_RESPONSE")
                .addKeyValue(HTTP_METHOD, request.getMethod())
                .addKeyValue(HTTP_PATH, path)
                .addKeyValue(HTTP_STATUS, statusCode)
                .addKeyValue(DURATION_MS, duration)
                .log("HTTP request completed successfully");
        }

        // 느린 요청 경고 (1초 이상)
        if (duration > 1000) {
            log.atWarn()
                .addKeyValue("event", "SLOW_REQUEST")
                .addKeyValue(HTTP_METHOD, request.getMethod())
                .addKeyValue(HTTP_PATH, path)
                .addKeyValue(DURATION_MS, duration)
                .log("Slow HTTP request detected");
        }
    }

    private boolean shouldSkipLogging(String path) {
        return path.startsWith("/actuator/") ||
               path.startsWith("/static/") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.startsWith("/favicon.ico") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs");
    }

    private void clearMDC() {
        MDC.clear();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("LoggingFilter initialized - MDC logging enabled");
    }

    @Override
    public void destroy() {
        log.info("LoggingFilter destroyed");
    }
}
