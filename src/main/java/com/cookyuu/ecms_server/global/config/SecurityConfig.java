package com.cookyuu.ecms_server.global.config;

import com.cookyuu.ecms_server.global.security.jwt.CustomAccessDeniedHandler;
import com.cookyuu.ecms_server.global.security.jwt.CustomAuthenticationEntryPoint;
import com.cookyuu.ecms_server.global.security.jwt.CustomUserDetailsService;
import com.cookyuu.ecms_server.global.security.jwt.JwtAuthFilter;
import com.cookyuu.ecms_server.global.utils.JwtUtils;
import com.cookyuu.ecms_server.global.utils.RedisUtils;
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
        "/api/v1/auth/**", "/api/v1/seller/registration", "api/v1/order/search"
    };
    private static final String[] AUTH_ADMIN = {
    };
    private static final String[] AUTH_SELLER = {
        "api/v1/product/registration", "/api/v1/product", "/api/v1/seller/info"
    };
    private static final String[] AUTH_USER = {

    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf((csrf) -> csrf.disable());
        http.cors(Customizer.withDefaults());

        http.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS));

        http.formLogin((form) -> form.disable());
        http.httpBasic(AbstractHttpConfigurer::disable);

        http.addFilterBefore(new JwtAuthFilter(customUserDetailsService, jwtUtils, redisUtils), UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(exceptionHandler -> exceptionHandler
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler));

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(AUTH_WHITELIST).permitAll()
                .requestMatchers(AUTH_ADMIN).hasRole("ADMIN")
                .requestMatchers(AUTH_SELLER).hasRole("SELLER")
                .requestMatchers(AUTH_USER).hasRole("USER")
                .anyRequest().authenticated());

        return http.build();
    }
}
