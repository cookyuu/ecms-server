package com.cookyuu.ecms_server.global.security.jwt;

import com.cookyuu.ecms_server.domain.member.entity.RoleType;
import com.cookyuu.ecms_server.global.code.RedisKeyCode;
import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.auth.ValidateJwtTokenException;
import com.cookyuu.ecms_server.global.utils.JwtUtils;
import com.cookyuu.ecms_server.global.utils.RedisUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
                }
                String logoutToken = redisUtils.getData(RedisKeyCode.LOGOUT_TOKEN.getSeparator()+ id);
                if (accessToken.equals(logoutToken)) {
                    throw new ValidateJwtTokenException(ResultCode.JWT_ALREADY_LOGOUT);
                }
            }
        } catch (Exception e) {
            request.setAttribute("exception", e);
        }
        filterChain.doFilter(request, response);
    }
}

