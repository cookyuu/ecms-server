package com.cookyuu.ecms_server.domain.auth.service;

import com.cookyuu.ecms_server.common.enums.CookieCode;
import com.cookyuu.ecms_server.common.enums.RedisKeyCode;
import com.cookyuu.ecms_server.common.utils.*;
import com.cookyuu.ecms_server.domain.auth.dto.JWTUserInfo;
import com.cookyuu.ecms_server.domain.auth.dto.LoginDto;
import com.cookyuu.ecms_server.domain.auth.dto.SignupDto;
import com.cookyuu.ecms_server.domain.cart.service.CartService;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.domain.seller.service.SellerService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    @Value("${auth.jwt.refresh.exp}")
    private String refreshTokenExp;
    @Value("${auth.jwt.access.exp}")
    private String accessTokenExp;

    private final int minute = 60;

    private final MemberService memberService;
    private final SellerService sellerService;
    private final CartService cartService;
    private final ValidateUtils validateUtils;
    private final AuthUtils authUtils;
    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;
    private final CookieUtils cookieUtils;

    @Transactional
    public void signupNormal(SignupDto.Request request) {
        String name = request.getName();
        String email = request.getEmail();
        String loginId = request.getLoginId();
        String phoneNumber = request.getPhoneNumber();
        String password = request.getPassword();
        String address = request.getAddress();

        validateProfileInfo(loginId, email, phoneNumber);
        Member registerMember = Member.of(name, email, loginId, validateAndEncryptPassword(password), phoneNumber, address);

        Member member = memberService.save(registerMember);

        cartService.makeCart(member);
    }

    @Transactional
    public LoginDto.Response loginNormal(LoginDto.Request request, HttpServletResponse response) {
        JWTUserInfo userInfo = memberService.checkLoginCredentials(request.getLoginId(), request.getPassword());

        log.atInfo()
            .addKeyValue(EVENT, TOKEN_ISSUED)
            .addKeyValue(USER_ID, userInfo.getId())
            .addKeyValue(LOGIN_ID, userInfo.getLoginId())
            .addKeyValue(USER_ROLE, userInfo.getRole())
            .addKeyValue(TOKEN_TYPE, "access/refresh")
            .log("Access and refresh tokens issued for normal user");

        String accessToken = jwtUtils.createAccessToken(userInfo);
        String refreshToken = jwtUtils.createRefreshToken(userInfo);
        Cookie cookie = cookieUtils.setCookieExpire(CookieCode.REFRESH_TOKEN, refreshToken, Integer.parseInt(refreshTokenExp));
        response.addCookie(cookie);

        redisUtils.setDataExpire(RedisKeyCode.REFRESH_TOKEN.getSeparator() + userInfo.getId(), refreshToken, Long.parseLong(refreshTokenExp)*minute);
        return LoginDto.Response.builder()
                .accessToken(accessToken)
                .build();
    }

    @Transactional
    public LoginDto.Response loginSeller(LoginDto.Request request, HttpServletResponse response) {
        JWTUserInfo userInfo = sellerService.checkLoginCredentials(request.getLoginId(), request.getPassword());

        log.atInfo()
            .addKeyValue(EVENT, TOKEN_ISSUED)
            .addKeyValue(USER_ID, userInfo.getId())
            .addKeyValue(LOGIN_ID, userInfo.getLoginId())
            .addKeyValue(USER_ROLE, userInfo.getRole())
            .addKeyValue(TOKEN_TYPE, "access/refresh")
            .log("Access and refresh tokens issued for seller");

        String accessToken = jwtUtils.createAccessToken((userInfo));
        String refreshToken = jwtUtils.createRefreshToken(userInfo);
        Cookie cookie = cookieUtils.setCookieExpire(CookieCode.REFRESH_TOKEN, refreshToken, Integer.parseInt(refreshTokenExp));
        response.addCookie(cookie);

        redisUtils.setDataExpire(RedisKeyCode.REFRESH_TOKEN.getSeparator() + userInfo.getId(), refreshToken, Long.parseLong(refreshTokenExp)*minute);
        return LoginDto.Response.builder()
                .accessToken(accessToken)
                .build();
    }

    @Transactional
    public void logoutNormal(UserDetails user, HttpServletRequest request, HttpServletResponse response) {
        String memberId = user.getUsername();
        String accessToken = jwtUtils.getAccessToken(request.getHeader("Authorization"));

        log.atInfo()
            .addKeyValue(EVENT, LOGOUT)
            .addKeyValue(USER_ID, memberId)
            .log("User logged out successfully");

        redisUtils.setDataExpire(RedisKeyCode.LOGOUT_TOKEN.getSeparator()+memberId, accessToken, Long.parseLong(accessTokenExp)*minute);
        redisUtils.deleteData(RedisKeyCode.REFRESH_TOKEN.getSeparator()+memberId);
        cookieUtils.removeCookie("refresh_token", response);
    }

    protected String validateAndEncryptPassword(String password) {
        validateUtils.isAvailablePasswordFormat(password);
        return authUtils.encryptPassword(password);
    }

    protected void validateProfileInfo(String loginId, String email, String phoneNumber) {
        memberService.checkDuplicateLoginId(loginId);
        validateUtils.isAvailableLoginIdFormat(loginId);

        memberService.checkDuplicateEmail(email);
        validateUtils.isAvailableEmailFormat(email);

        memberService.checkDuplicatePhoneNumber(phoneNumber);
        validateUtils.isAvailablePhoneNumberFormat(phoneNumber);
    }

}
