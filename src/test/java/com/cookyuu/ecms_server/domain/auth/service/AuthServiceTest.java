package com.cookyuu.ecms_server.domain.auth.service;

import com.cookyuu.ecms_server.common.enums.CookieCode;
import com.cookyuu.ecms_server.common.enums.RedisKeyCode;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.utils.*;
import com.cookyuu.ecms_server.domain.auth.dto.JWTUserInfo;
import com.cookyuu.ecms_server.domain.auth.dto.LoginDto;
import com.cookyuu.ecms_server.domain.auth.dto.SignupDto;
import com.cookyuu.ecms_server.domain.cart.entity.Cart;
import com.cookyuu.ecms_server.domain.cart.service.CartService;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.domain.seller.service.SellerService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberService memberService;

    @Mock
    private SellerService sellerService;

    @Mock
    private CartService cartService;

    @Spy
    private ValidateUtils validateUtils;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RedisUtils redisUtils;

    @Mock
    private CookieUtils cookieUtils;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    // ==================== signupNormal 테스트 ====================

    @DisplayName("정상적인 회원정보가 주어질 때 회원가입을 하면 성공한다")
    @Test
    void givenValidSignupInfo_whenSignupNormal_thenSuccess() {
        // Given
        SignupDto.Request request = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("test1234")
                .password("test123!@#")
                .phoneNumber("010-1234-5678")
                .address("서울특별시 강남구")
                .build();

        String encryptedPassword = "encrypted_password";
        when(authUtils.encryptPassword(anyString())).thenReturn(encryptedPassword);

        Member savedMember = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .loginId(request.getLoginId())
                .password(encryptedPassword)
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .role(RoleType.USER)
                .build();
        ReflectionTestUtils.setField(savedMember, "id", 1L);

        when(memberService.save(any(Member.class))).thenReturn(savedMember);

        // When
        authService.signupNormal(request);

        // Then
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberService, times(1)).checkDuplicateLoginId(request.getLoginId());
        verify(memberService, times(1)).checkDuplicateEmail(request.getEmail());
        verify(memberService, times(1)).checkDuplicatePhoneNumber(request.getPhoneNumber());
        verify(authUtils, times(1)).encryptPassword(request.getPassword());
        verify(memberService, times(1)).save(memberCaptor.capture());
        verify(cartService, times(1)).makeCart(savedMember);

        Member capturedMember = memberCaptor.getValue();
        assertThat(capturedMember.getName()).isEqualTo(request.getName());
        assertThat(capturedMember.getEmail()).isEqualTo(request.getEmail());
        assertThat(capturedMember.getLoginId()).isEqualTo(request.getLoginId());
        assertThat(capturedMember.getPassword()).isEqualTo(encryptedPassword);
        assertThat(capturedMember.getPhoneNumber()).isEqualTo(request.getPhoneNumber());
        assertThat(capturedMember.getAddress()).isEqualTo(request.getAddress());
        assertThat(capturedMember.getRole()).isEqualTo(RoleType.USER);
    }

    @DisplayName("중복된 로그인 아이디가 주어질 때 회원가입을 하면 예외가 발생한다")
    @Test
    void givenDuplicateLoginId_whenSignupNormal_thenThrowException() {
        // Given
        SignupDto.Request request = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("duplicate")
                .password("test123!@#")
                .phoneNumber("010-1234-5678")
                .address("서울특별시 강남구")
                .build();

        doThrow(new BusinessException(ResultCode.VALID_LOGINID_DUPLICATE))
                .when(memberService).checkDuplicateLoginId(request.getLoginId());

        // When & Then
        assertThatThrownBy(() -> authService.signupNormal(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.VALID_LOGINID_DUPLICATE);

        verify(memberService, times(1)).checkDuplicateLoginId(request.getLoginId());
        verify(memberService, never()).save(any(Member.class));
    }

    @DisplayName("잘못된 이메일 형식이 주어질 때 회원가입을 하면 예외가 발생한다")
    @Test
    void givenInvalidEmail_whenSignupNormal_thenThrowException() {
        // Given
        SignupDto.Request request = SignupDto.Request.builder()
                .name("테스트이름")
                .email("invalid-email")
                .loginId("test1234")
                .password("test123!@#")
                .phoneNumber("010-1234-5678")
                .address("서울특별시 강남구")
                .build();

        // When & Then
        assertThatThrownBy(() -> authService.signupNormal(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.VALID_EMAIL_FORMAT)
                .hasMessageContaining(ResultCode.VALID_EMAIL_FORMAT.getMessage());

        verify(memberService, never()).save(any(Member.class));
    }

    @DisplayName("잘못된 로그인 아이디 형식이 주어질 때 회원가입을 하면 예외가 발생한다")
    @Test
    void givenInvalidLoginIdFormat_whenSignupNormal_thenThrowException() {
        // Given - 너무 짧은 로그인 아이디 (6자 미만)
        SignupDto.Request request = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("abc")
                .password("test123!@#")
                .phoneNumber("010-1234-5678")
                .address("서울특별시 강남구")
                .build();

        // When & Then
        assertThatThrownBy(() -> authService.signupNormal(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.VALID_LOGINID_FORMAT);

        verify(memberService, never()).save(any(Member.class));
    }

    @DisplayName("잘못된 전화번호 형식이 주어질 때 회원가입을 하면 예외가 발생한다")
    @Test
    void givenInvalidPhoneNumber_whenSignupNormal_thenThrowException() {
        // Given
        SignupDto.Request request = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("test1234")
                .password("test123!@#")
                .phoneNumber("010-12-3456")
                .address("서울특별시 강남구")
                .build();

        // When & Then
        assertThatThrownBy(() -> authService.signupNormal(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.VALID_PHONENUMBER_FORMAT);

        verify(memberService, never()).save(any(Member.class));
    }

    @DisplayName("잘못된 비밀번호 형식이 주어질 때 회원가입을 하면 예외가 발생한다")
    @Test
    void givenInvalidPassword_whenSignupNormal_thenThrowException() {
        // Given - 특수문자 없는 비밀번호
        SignupDto.Request request = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("test1234")
                .password("test1234")
                .phoneNumber("010-1234-5678")
                .address("서울특별시 강남구")
                .build();

        // When & Then
        assertThatThrownBy(() -> authService.signupNormal(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.VALID_PASSWORD_FORMAT);

        verify(memberService, never()).save(any(Member.class));
    }

    // ==================== loginNormal 테스트 ====================

    @DisplayName("올바른 일반 회원 로그인 정보가 주어질 때 로그인을 하면 성공한다")
    @Test
    void givenValidCredentials_whenLoginNormal_thenSuccess() {
        // Given
        LoginDto.Request loginRequest = new LoginDto.Request("test1234", "test123!@#");

        JWTUserInfo userInfo = JWTUserInfo.builder()
                .id(1L)
                .loginId("test1234")
                .role(RoleType.USER)
                .build();

        String accessToken = "access_token_sample";
        String refreshToken = "refresh_token_sample";
        Cookie cookie = new Cookie(CookieCode.REFRESH_TOKEN.name(), refreshToken);

        ReflectionTestUtils.setField(authService, "refreshTokenExp", "3600");

        when(memberService.checkLoginCredentials(loginRequest.getLoginId(), loginRequest.getPassword()))
                .thenReturn(userInfo);
        when(jwtUtils.createAccessToken(userInfo)).thenReturn(accessToken);
        when(jwtUtils.createRefreshToken(userInfo)).thenReturn(refreshToken);
        when(cookieUtils.setCookieExpire(CookieCode.REFRESH_TOKEN, refreshToken, 3600)).thenReturn(cookie);

        // When
        LoginDto.Response result = authService.loginNormal(loginRequest, response);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(accessToken);

        verify(memberService, times(1)).checkLoginCredentials(loginRequest.getLoginId(), loginRequest.getPassword());
        verify(jwtUtils, times(1)).createAccessToken(userInfo);
        verify(jwtUtils, times(1)).createRefreshToken(userInfo);
        verify(cookieUtils, times(1)).setCookieExpire(CookieCode.REFRESH_TOKEN, refreshToken, 3600);
        verify(response, times(1)).addCookie(cookie);
        verify(redisUtils, times(1)).setDataExpire(
                eq(RedisKeyCode.REFRESH_TOKEN.getSeparator() + userInfo.getId()),
                eq(refreshToken),
                eq(3600L * 60)
        );
    }

    @DisplayName("잘못된 로그인 정보가 주어질 때 일반 회원 로그인을 하면 예외가 발생한다")
    @Test
    void givenInvalidCredentials_whenLoginNormal_thenThrowException() {
        // Given
        LoginDto.Request loginRequest = new LoginDto.Request("test1234", "wrongPassword");

        when(memberService.checkLoginCredentials(loginRequest.getLoginId(), loginRequest.getPassword()))
                .thenThrow(new BusinessException(ResultCode.MEMBER_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> authService.loginNormal(loginRequest, response))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.MEMBER_NOT_FOUND);

        verify(memberService, times(1)).checkLoginCredentials(loginRequest.getLoginId(), loginRequest.getPassword());
        verify(jwtUtils, never()).createAccessToken(any());
        verify(jwtUtils, never()).createRefreshToken(any());
    }

    // ==================== loginSeller 테스트 ====================

    @DisplayName("올바른 판매자 로그인 정보가 주어질 때 로그인을 하면 성공한다")
    @Test
    void givenValidSellerCredentials_whenLoginSeller_thenSuccess() {
        // Given
        LoginDto.Request loginRequest = new LoginDto.Request("seller123", "seller123!@#");

        JWTUserInfo userInfo = JWTUserInfo.builder()
                .id(1L)
                .loginId("seller123")
                .role(RoleType.SELLER)
                .build();

        String accessToken = "seller_access_token";
        String refreshToken = "seller_refresh_token";
        Cookie cookie = new Cookie(CookieCode.REFRESH_TOKEN.name(), refreshToken);

        ReflectionTestUtils.setField(authService, "refreshTokenExp", "3600");

        when(sellerService.checkLoginCredentials(loginRequest.getLoginId(), loginRequest.getPassword()))
                .thenReturn(userInfo);
        when(jwtUtils.createAccessToken(userInfo)).thenReturn(accessToken);
        when(jwtUtils.createRefreshToken(userInfo)).thenReturn(refreshToken);
        when(cookieUtils.setCookieExpire(CookieCode.REFRESH_TOKEN, refreshToken, 3600)).thenReturn(cookie);

        // When
        LoginDto.Response result = authService.loginSeller(loginRequest, response);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(accessToken);

        verify(sellerService, times(1)).checkLoginCredentials(loginRequest.getLoginId(), loginRequest.getPassword());
        verify(jwtUtils, times(1)).createAccessToken(userInfo);
        verify(jwtUtils, times(1)).createRefreshToken(userInfo);
        verify(cookieUtils, times(1)).setCookieExpire(CookieCode.REFRESH_TOKEN, refreshToken, 3600);
        verify(response, times(1)).addCookie(cookie);
        verify(redisUtils, times(1)).setDataExpire(
                eq(RedisKeyCode.REFRESH_TOKEN.getSeparator() + userInfo.getId()),
                eq(refreshToken),
                eq(3600L * 60)
        );
    }

    @DisplayName("잘못된 로그인 정보가 주어질 때 판매자 로그인을 하면 예외가 발생한다")
    @Test
    void givenInvalidSellerCredentials_whenLoginSeller_thenThrowException() {
        // Given
        LoginDto.Request loginRequest = new LoginDto.Request("seller123", "wrongPassword");

        when(sellerService.checkLoginCredentials(loginRequest.getLoginId(), loginRequest.getPassword()))
                .thenThrow(new BusinessException(ResultCode.SELLER_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> authService.loginSeller(loginRequest, response))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.SELLER_NOT_FOUND);

        verify(sellerService, times(1)).checkLoginCredentials(loginRequest.getLoginId(), loginRequest.getPassword());
        verify(jwtUtils, never()).createAccessToken(any());
        verify(jwtUtils, never()).createRefreshToken(any());
    }

    // ==================== logoutNormal 테스트 ====================

    @DisplayName("인증된 사용자가 로그아웃을 하면 성공한다")
    @Test
    void givenAuthenticatedUser_whenLogoutNormal_thenSuccess() {
        // Given
        String memberId = "1";
        String accessToken = "access_token_sample";
        String authorizationHeader = "Bearer " + accessToken;

        UserDetails userDetails = User.builder()
                .username(memberId)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        ReflectionTestUtils.setField(authService, "accessTokenExp", "1800");

        when(request.getHeader("Authorization")).thenReturn(authorizationHeader);
        when(jwtUtils.getAccessToken(authorizationHeader)).thenReturn(accessToken);

        // When
        authService.logoutNormal(userDetails, request, response);

        // Then
        verify(jwtUtils, times(1)).getAccessToken(authorizationHeader);
        verify(redisUtils, times(1)).setDataExpire(
                eq(RedisKeyCode.LOGOUT_TOKEN.getSeparator() + memberId),
                eq(accessToken),
                eq(1800L * 60)
        );
        verify(redisUtils, times(1)).deleteData(RedisKeyCode.REFRESH_TOKEN.getSeparator() + memberId);
        verify(cookieUtils, times(1)).removeCookie("refresh_token", response);
    }

    @DisplayName("Authorization 헤더가 없을 때 로그아웃을 하면 정상 처리된다")
    @Test
    void givenNoAuthorizationHeader_whenLogoutNormal_thenStillSuccess() {
        // Given
        String memberId = "1";

        UserDetails userDetails = User.builder()
                .username(memberId)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        ReflectionTestUtils.setField(authService, "accessTokenExp", "1800");

        when(request.getHeader("Authorization")).thenReturn(null);
        when(jwtUtils.getAccessToken(null)).thenReturn(null);

        // When
        authService.logoutNormal(userDetails, request, response);

        // Then
        verify(jwtUtils, times(1)).getAccessToken(null);
        verify(redisUtils, times(1)).setDataExpire(
                eq(RedisKeyCode.LOGOUT_TOKEN.getSeparator() + memberId),
                eq(null),
                eq(1800L * 60)
        );
        verify(redisUtils, times(1)).deleteData(RedisKeyCode.REFRESH_TOKEN.getSeparator() + memberId);
        verify(cookieUtils, times(1)).removeCookie("refresh_token", response);
    }

    // ==================== validateAndEncryptPassword 테스트 (간접 테스트) ====================

    @DisplayName("올바른 비밀번호가 주어질 때 검증 후 암호화를 하면 암호화된 비밀번호를 반환한다")
    @Test
    void givenValidPassword_whenValidateAndEncrypt_thenReturnEncryptedPassword() {
        // Given
        SignupDto.Request request = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("test1234")
                .password("valid123!@#")
                .phoneNumber("010-1234-5678")
                .address("서울특별시 강남구")
                .build();

        String encryptedPassword = "encrypted_valid123";
        when(authUtils.encryptPassword(anyString())).thenReturn(encryptedPassword);

        Member savedMember = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .loginId(request.getLoginId())
                .password(encryptedPassword)
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .role(RoleType.USER)
                .build();
        ReflectionTestUtils.setField(savedMember, "id", 1L);

        when(memberService.save(any(Member.class))).thenReturn(savedMember);

        // When
        authService.signupNormal(request);

        // Then
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberService, times(1)).save(memberCaptor.capture());
        verify(authUtils, times(1)).encryptPassword(request.getPassword());

        Member capturedMember = memberCaptor.getValue();
        assertThat(capturedMember.getPassword()).isEqualTo(encryptedPassword);
    }

    // ==================== validateProfileInfo 테스트 (간접 테스트) ====================

    @DisplayName("중복된 이메일이 주어질 때 회원가입을 하면 예외가 발생한다")
    @Test
    void givenDuplicateEmail_whenSignupNormal_thenThrowException() {
        // Given
        SignupDto.Request request = SignupDto.Request.builder()
                .name("테스트이름")
                .email("duplicate@test.com")
                .loginId("test1234")
                .password("test123!@#")
                .phoneNumber("010-1234-5678")
                .address("서울특별시 강남구")
                .build();

        doThrow(new BusinessException(ResultCode.VALID_EMAIL_DUPLICATE))
                .when(memberService).checkDuplicateEmail(request.getEmail());

        // When & Then
        assertThatThrownBy(() -> authService.signupNormal(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.VALID_EMAIL_DUPLICATE);

        verify(memberService, times(1)).checkDuplicateLoginId(request.getLoginId());
        verify(memberService, times(1)).checkDuplicateEmail(request.getEmail());
        verify(memberService, never()).save(any(Member.class));
    }

    @DisplayName("중복된 전화번호가 주어질 때 회원가입을 하면 예외가 발생한다")
    @Test
    void givenDuplicatePhoneNumber_whenSignupNormal_thenThrowException() {
        // Given
        SignupDto.Request request = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("test1234")
                .password("test123!@#")
                .phoneNumber("010-9999-9999")
                .address("서울특별시 강남구")
                .build();

        doThrow(new BusinessException(ResultCode.VALID_PHONENUMBER_DUPLICATE))
                .when(memberService).checkDuplicatePhoneNumber(request.getPhoneNumber());

        // When & Then
        assertThatThrownBy(() -> authService.signupNormal(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.VALID_PHONENUMBER_DUPLICATE);

        verify(memberService, times(1)).checkDuplicateLoginId(request.getLoginId());
        verify(memberService, times(1)).checkDuplicateEmail(request.getEmail());
        verify(memberService, times(1)).checkDuplicatePhoneNumber(request.getPhoneNumber());
        verify(memberService, never()).save(any(Member.class));
    }
}
