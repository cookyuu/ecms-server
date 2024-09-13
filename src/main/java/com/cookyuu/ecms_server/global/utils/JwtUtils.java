package com.cookyuu.ecms_server.global.utils;

import com.cookyuu.ecms_server.domain.auth.dto.JWTUserInfo;
import com.cookyuu.ecms_server.global.dto.RedisKeyCode;
import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.auth.UserLoginException;
import com.cookyuu.ecms_server.global.exception.auth.ValidateJwtTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {
    private final Key key;
    private final long accessTokenExpTime;
    private final long refreshTokenExpTime;
    private final RedisUtils redisUtils;

    public JwtUtils(@Value("${auth.jwt.secret}") String secretKey,
                    @Value("${auth.jwt.access.exp}") String accessTokenExpTime,
                    @Value("${auth.jwt.refresh.exp}") String refreshTokenExpTime,
                    RedisUtils redisUtils)
    {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpTime = Long.parseLong(accessTokenExpTime);
        this.refreshTokenExpTime = Long.parseLong(refreshTokenExpTime);
        this.redisUtils = redisUtils;
    }

    public String createAccessToken(JWTUserInfo userInfo) {
        return createToken(userInfo, accessTokenExpTime);
    }
    public String createRefreshToken(JWTUserInfo userInfo) {
        return createToken(userInfo, refreshTokenExpTime);
    }

    private String createToken(JWTUserInfo member, long expiredTime) {
        log.debug("[CreateJwtToken] Claims (member id :{}, loginId : {}, role : {}) ", member.getId(), member.getLoginId(), member.getRole());

        Claims claims = Jwts.claims();
        claims.put("memberId", member.getId());
        claims.put("loginId", member.getLoginId());
        claims.put("role", member.getRole());

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tokenValidity = now.plusSeconds(expiredTime);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(tokenValidity.toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getId(String token) {
        return parseClaims(token).get("memberId", Long.class);
    }

    public String getLoginId(String token) {
        return parseClaims(token).get("loginId", String.class);
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public String getAccessToken(String authorization) {
        return authorization.substring(7);
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public void isLogoutToken(Long memberId, String accessToken) {
        log.debug("[CheckRedisCache] Logout token check");
        try {
            String logoutToken = redisUtils.getData(RedisKeyCode.LOGOUT_TOKEN.getSeparator() + memberId);
            if (logoutToken != null && logoutToken.equals(accessToken)) {
                throw new UserLoginException(ResultCode.ALREADY_LOGOUT_USER);
            }
        } catch (Exception e) {
            throw new UserLoginException(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    public void validateToken(String token) {
        try {
            log.debug("[ValidateJwtToken] Validate accessToken. ");
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch (SecurityException e) {
            log.info("[ValidateJwtToken] Invalid JWT Token. ", e);
            throw new ValidateJwtTokenException(ResultCode.JWT_SECURE_EXP);
        } catch (MalformedJwtException e) {
            log.info("[ValidateJwtToken] Invalid JWT Token. ", e);
            throw new ValidateJwtTokenException(ResultCode.JWT_INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            log.info("[ValidateJwtToken] Expired JWT Token. ", e);
            throw new ValidateJwtTokenException(ResultCode.JWT_EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.info("[ValidateJwtToken] Unsupported JWT Token. ", e);
            throw new ValidateJwtTokenException(ResultCode.JWT_UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            log.info("[ValidateJwtToken] JWT Claims String is empty. ", e);
            throw new ValidateJwtTokenException(ResultCode.JWT_EMPTY_TOKEN);
        } catch (Exception e) {
            throw new UserLoginException(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

}
