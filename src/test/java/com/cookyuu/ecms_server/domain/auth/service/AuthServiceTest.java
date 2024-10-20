package com.cookyuu.ecms_server.domain.auth.service;

import com.cookyuu.ecms_server.domain.auth.dto.SignupDto;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.auth.*;
import com.cookyuu.ecms_server.global.utils.AuthUtils;
import com.cookyuu.ecms_server.global.utils.ValidateUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberService memberService;
    @Spy
    private ValidateUtils validateUtils;
    @Mock
    private AuthUtils authUtils;

    @DisplayName("정상 로그인 테스트")
    @Test
    void givenNormalInfo_whenSignup_thenReturnTrue() {
        SignupDto.Request request = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("test123")
                .password("test123!@#")
                .phoneNumber("010-0000-0000")
                .address("테스트 주소")
                .build();

        Member mockMember = mock(Member.class);
        when(memberService.save(any(Member.class))).thenReturn(mockMember);

        authService.signupNormal(request);

        verify(memberService, times(1)).save(any(Member.class));
    }

    @DisplayName("회원가입 예외 발생 테스트 (이메일)")
    @Test
    void givenInvalidEmail_whenSignup_thenThrowException() {
        SignupDto.Request request1 = SignupDto.Request.builder()
                .name("테스트이름")
                .email("testtest.com")
                .loginId("test123")
                .password("test123!@#")
                .phoneNumber("010-0000-0000")
                .address("테스트 주소")
                .build();
        SignupDto.Request request2 = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@testcom")
                .loginId("test123")
                .password("test123!@#")
                .phoneNumber("010-0000-0000")
                .address("테스트 주소")
                .build();

        ValidationException exception1 = assertThrows(ValidationException.class, () -> {
            authService.signupNormal(request1);
        });
        ValidationException exception2 = assertThrows(ValidationException.class, () -> {
            authService.signupNormal(request2);
        });
        assertEquals(ResultCode.VALID_EMAIL_FORMAT.getMessage(), exception1.getResultCode().getMessage());
        assertEquals(ResultCode.VALID_EMAIL_FORMAT.getMessage(), exception2.getResultCode().getMessage());

        verify(memberService, never()).save(any(Member.class));
    }

    @DisplayName("회원가입 예외 발생 테스트 (유저 아이디)")
    @Test
    void givenInvalidUserId_whenSignup_thenThrowException() {
        SignupDto.Request request1 = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("test12345678901")
                .password("test123!@#")
                .phoneNumber("010-0000-0000")
                .address("테스트 주소")
                .build();
        SignupDto.Request request2 = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("1est1789")
                .password("test123!@#")
                .phoneNumber("010-0000-0000")
                .address("테스트 주소")
                .build();
        SignupDto.Request request3 = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("test1")
                .password("test123!@#")
                .phoneNumber("010-0000-0000")
                .address("테스트 주소")
                .build();

        ValidationException exception1 = assertThrows(ValidationException.class, () -> {
            authService.signupNormal(request1);
        });
        ValidationException exception2 = assertThrows(ValidationException.class, () -> {
            authService.signupNormal(request2);
        });
        ValidationException exception3 = assertThrows(ValidationException.class, () -> {
            authService.signupNormal(request3);
        });
        assertEquals(ResultCode.VALID_LOGINID_FORMAT.getMessage(), exception1.getResultCode().getMessage());
        assertEquals(ResultCode.VALID_LOGINID_FORMAT.getMessage(), exception2.getResultCode().getMessage());
        assertEquals(ResultCode.VALID_LOGINID_FORMAT.getMessage(), exception3.getResultCode().getMessage());

        verify(memberService, never()).save(any(Member.class));
    }

    @DisplayName("회원가입 예외 발생 테스트 (핸드폰 번호)")
    @Test
    void givenInvalidPhoneNumber_whenSignup_thenThrowException() {
        SignupDto.Request request1 = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("test1234")
                .password("test123!@#")
                .phoneNumber("01-0000-0000")
                .address("테스트 주소")
                .build();
        SignupDto.Request request2 = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("test1234")
                .password("test123!@#")
                .phoneNumber("010-00-0000")
                .address("테스트 주소")
                .build();
        SignupDto.Request request3 = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("test1234")
                .password("test123!@#")
                .phoneNumber("010-000-000")
                .address("테스트 주소")
                .build();
        SignupDto.Request request4 = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("test1234")
                .password("test123!@#")
                .phoneNumber("010-000-000A")
                .address("테스트 주소")
                .build();

        ValidationException exception1 = assertThrows(ValidationException.class, () -> {
            authService.signupNormal(request1);
        });
        ValidationException exception2 = assertThrows(ValidationException.class, () -> {
            authService.signupNormal(request2);
        });
        ValidationException exception3 = assertThrows(ValidationException.class, () -> {
            authService.signupNormal(request3);
        });
        ValidationException exception4 = assertThrows(ValidationException.class, () -> {
            authService.signupNormal(request4);
        });

        assertEquals(ResultCode.VALID_PHONENUMBER_FORMAT.getMessage(), exception1.getResultCode().getMessage());
        assertEquals(ResultCode.VALID_PHONENUMBER_FORMAT.getMessage(), exception2.getResultCode().getMessage());
        assertEquals(ResultCode.VALID_PHONENUMBER_FORMAT.getMessage(), exception3.getResultCode().getMessage());
        assertEquals(ResultCode.VALID_PHONENUMBER_FORMAT.getMessage(), exception4.getResultCode().getMessage());

        verify(memberService, never()).save(any(Member.class));
    }

    @DisplayName("회원가입 예외 발생 테스트 (패스워드)")
    @Test
    void givenInvalidPassword_whenSignup_thenThrowException() {
        SignupDto.Request request1 = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("test1234")
                .password("test123123")
                .phoneNumber("010-0000-0000")
                .address("테스트 주소")
                .build();
        SignupDto.Request request2 = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("test1234")
                .password("testtest!@#")
                .phoneNumber("010-000-0000")
                .address("테스트 주소")
                .build();
        SignupDto.Request request3 = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("test1234")
                .password("123123!@#")
                .phoneNumber("010-000-0000")
                .address("테스트 주소")
                .build();
        SignupDto.Request request4 = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("test1234")
                .password("te3!@##")
                .phoneNumber("010-000-0000")
                .address("테스트 주소")
                .build();
        SignupDto.Request request5 = SignupDto.Request.builder()
                .name("테스트이름")
                .email("test@test.com")
                .loginId("test1234")
                .password("test12345678!@##")
                .phoneNumber("010-000-0000")
                .address("테스트 주소")
                .build();

        ValidationException exception1 = assertThrows(ValidationException.class, () -> {
            authService.signupNormal(request1);
        });
        ValidationException exception2 = assertThrows(ValidationException.class, () -> {
            authService.signupNormal(request2);
        });
        ValidationException exception3 = assertThrows(ValidationException.class, () -> {
            authService.signupNormal(request3);
        });
        ValidationException exception4 = assertThrows(ValidationException.class, () -> {
            authService.signupNormal(request4);
        });
        ValidationException exception5 = assertThrows(ValidationException.class, () -> {
            authService.signupNormal(request5);
        });

        assertEquals(ResultCode.VALID_PASSWORD_FORMAT.getMessage(), exception1.getResultCode().getMessage());
        assertEquals(ResultCode.VALID_PASSWORD_FORMAT.getMessage(), exception2.getResultCode().getMessage());
        assertEquals(ResultCode.VALID_PASSWORD_FORMAT.getMessage(), exception3.getResultCode().getMessage());
        assertEquals(ResultCode.VALID_PASSWORD_FORMAT.getMessage(), exception4.getResultCode().getMessage());
        assertEquals(ResultCode.VALID_PASSWORD_FORMAT.getMessage(), exception5.getResultCode().getMessage());

        verify(memberService, never()).save(any(Member.class));
    }

}