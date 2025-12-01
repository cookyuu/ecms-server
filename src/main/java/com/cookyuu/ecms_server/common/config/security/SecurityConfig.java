package com.cookyuu.ecms_server.common.config.security;

import com.cookyuu.ecms_server.common.security.jwt.CustomAccessDeniedHandler;
import com.cookyuu.ecms_server.common.security.jwt.CustomAuthenticationEntryPoint;
import com.cookyuu.ecms_server.common.security.userdetails.CustomUserDetailsService;
import com.cookyuu.ecms_server.common.security.jwt.JwtAuthFilter;
import com.cookyuu.ecms_server.common.utils.JwtUtils;
import com.cookyuu.ecms_server.common.utils.RedisUtils;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@AllArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    private static final String[] AUTH_WHITELIST = {
        "/api/v1/auth/signup",
        "/api/v1/auth/login",
        "/api/v1/auth/login/seller",
        "/api/v1/auth/validation/**",
        "/api/v1/seller/registration",
        "/api/v1/member/role",
        "/swagger-ui/**",
        "/swagger-resources/**",
        "/v3/api-docs/**"
    };

    private static final String[] AUTH_ADMIN = {
        "/api/v1/admin/**",
        "/api/v1/category/**"
    };

    private static final String[] AUTH_SELLER = {
        "/api/v1/seller/**"
    };

    private static final String[] AUTH_USER = {
        "/api/v1/order/**",
        "/api/v1/payment/**",
        "/api/v1/cart/**",
        "/api/v1/coupon/**",
        "/api/v1/user/**",
        "/api/v1/member/**"
    };

    // HTTP 메서드별 권한이 다른 경로들
    private static final String PRODUCT_PATH = "/api/v1/product/**";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf((csrf) -> csrf.disable());
        http.cors(Customizer.withDefaults());

        http.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS));

        http.formLogin((form) -> form.disable());
        http.httpBasic(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(AUTH_WHITELIST).permitAll()
                .requestMatchers("GET", PRODUCT_PATH).permitAll() // 상품 조회는 모두 허용
                .requestMatchers("POST", PRODUCT_PATH).hasAnyRole("SELLER", "ADMIN") // 상품 등록은 판매자/관리자만
                .requestMatchers("PUT", PRODUCT_PATH).hasAnyRole("SELLER", "ADMIN") // 상품 수정은 판매자/관리자만
                .requestMatchers("DELETE", PRODUCT_PATH).hasAnyRole("SELLER", "ADMIN") // 상품 삭제는 판매자/관리자만
                .requestMatchers(AUTH_ADMIN).hasRole("ADMIN") // ADMIN 전용 (category, member/role 포함)
                .requestMatchers(AUTH_SELLER).hasAnyRole("SELLER", "ADMIN")
                .requestMatchers(AUTH_USER).hasAnyRole("USER", "SELLER", "ADMIN")
                .anyRequest().authenticated() // 나머지는 모두 인증 필요
        );

        http.addFilterBefore(new JwtAuthFilter(customUserDetailsService, jwtUtils, redisUtils), UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(exceptionHandler -> exceptionHandler
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler));
        return http.build();
    }
}
