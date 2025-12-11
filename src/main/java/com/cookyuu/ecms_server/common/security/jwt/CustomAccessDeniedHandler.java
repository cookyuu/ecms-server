package com.cookyuu.ecms_server.common.security.jwt;

import com.cookyuu.ecms_server.common.web.dto.ApiResponse;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.web.filter.RequestContextFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j(topic = "FORBIDDEN_EXCEPTION_HANDLER")
@AllArgsConstructor
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("No Authorities", accessDeniedException);

        // 1. 요청 경로 추출
        String path = extractRequestPath(request);

        // 2. traceId 추출
        String traceId = extractTraceId(request);

        // 3. ApiResponse 생성 및 컨텍스트 정보 추가
        ApiResponse<Object> errorResponse = ApiResponse.failure(
                ResultCode.ACCESS_DENIED,
                "[ValidateJwtToken] Token verification failed. You do not have permission. ErrorMsg : " + accessDeniedException.getMessage()
        ).withContext(path, traceId);

        String resBody = objectMapper.writeValueAsString(errorResponse);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(resBody);
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
