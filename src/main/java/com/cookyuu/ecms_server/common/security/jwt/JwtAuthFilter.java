package com.cookyuu.ecms_server.common.security.jwt;

import com.cookyuu.ecms_server.common.enums.RedisKeyCode;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.AuthenticationException;
import com.cookyuu.ecms_server.common.security.userdetails.CustomUserDetailsService;
import com.cookyuu.ecms_server.common.utils.JwtUtils;
import com.cookyuu.ecms_server.common.utils.RedisUtils;
import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.cookyuu.ecms_server.common.logging.LogFields.USER_ID;
import static com.cookyuu.ecms_server.common.logging.LogFields.USER_ROLE;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;
    /*
     * JWT 토큰 검증 필터
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String authorizationHeader = request.getHeader("Authorization");
            log.info("[ValidateJwtToken] Authorization Code : {}", authorizationHeader);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String accessToken = jwtUtils.getAccessToken(authorizationHeader);
                RoleType role = RoleType.valueOf(jwtUtils.getRole(accessToken));
                log.debug("[ValidateJwtToken] Role Type : {}", role);
                jwtUtils.validateToken(accessToken);
                Long id = jwtUtils.getId(accessToken);
                jwtUtils.isLogoutToken(id, accessToken);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(id.toString(), role);
                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, accessToken, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

                    // MDC에 user_id와 user_role 추가 (모든 로그에 자동 포함됨)
                    MDC.put(USER_ID, id.toString());
                    MDC.put(USER_ROLE, role.name());
                }
                String logoutToken = redisUtils.getData(RedisKeyCode.LOGOUT_TOKEN.getSeparator()+ id);
                if (accessToken.equals(logoutToken)) {
                    throw new AuthenticationException(ResultCode.JWT_ALREADY_LOGOUT);
                }
            }
        } catch (Exception e) {
            request.setAttribute("exception", e);
        }
        filterChain.doFilter(request, response);
    }
}

