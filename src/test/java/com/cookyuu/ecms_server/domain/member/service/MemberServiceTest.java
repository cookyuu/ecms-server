package com.cookyuu.ecms_server.domain.member.service;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.AuthenticationException;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.utils.AuthUtils;
import com.cookyuu.ecms_server.domain.auth.dto.JWTUserInfo;
import com.cookyuu.ecms_server.domain.member.dto.MemberDetailDto;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import com.cookyuu.ecms_server.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuthUtils authUtils;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    // ==================== save 테스트 ====================

    @DisplayName("유효한 회원 정보가 주어질 때 저장을 하면 성공한다")
    @Test
    void givenValidMember_whenSave_thenSuccess() {
        // Given
        Member member = Member.builder()
                .name("테스트유저")
                .email("test@test.com")
                .loginId("test1234")
                .password("encryptedPassword")
                .phoneNumber("010-1234-5678")
                .address("서울특별시 강남구")
                .role(RoleType.USER)
                .build();

        Member savedMember = Member.builder()
                .name("테스트유저")
                .email("test@test.com")
                .loginId("test1234")
                .password("encryptedPassword")
                .phoneNumber("010-1234-5678")
                .address("서울특별시 강남구")
                .role(RoleType.USER)
                .build();
        ReflectionTestUtils.setField(savedMember, "id", 1L);

        when(memberRepository.save(member)).thenReturn(savedMember);

        // When
        Member result = memberService.save(member);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("테스트유저");
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getLoginId()).isEqualTo("test1234");
        assertThat(result.getRole()).isEqualTo(RoleType.USER);

        verify(memberRepository, times(1)).save(member);
    }

    @DisplayName("중복된 회원 정보가 주어질 때 저장을 하면 예외가 발생한다")
    @Test
    void givenDuplicateMember_whenSave_thenThrowException() {
        // Given
        Member member = Member.builder()
                .name("테스트유저")
                .email("test@test.com")
                .loginId("duplicate")
                .password("encryptedPassword")
                .phoneNumber("010-1234-5678")
                .address("서울특별시 강남구")
                .role(RoleType.USER)
                .build();

        when(memberRepository.save(member)).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        // When & Then
        assertThatThrownBy(() -> memberService.save(member))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Duplicate entry");

        verify(memberRepository, times(1)).save(member);
    }

    // ==================== getMemberDetail 테스트 ====================

    @DisplayName("로그인 아이디가 주어질 때 회원 상세 조회를 하면 성공한다")
    @Test
    void givenLoginId_whenGetMemberDetail_thenSuccess() {
        // Given
        String loginId = "test1234";
        MemberDetailDto memberDetail = MemberDetailDto.builder()
                .id(1L)
                .name("테스트유저")
                .email("test@test.com")
                .loginId(loginId)
                .phoneNumber("010-1234-5678")
                .address("서울특별시 강남구")
                .role(RoleType.USER)
                .build();

        when(memberRepository.getMemberDetail(loginId)).thenReturn(memberDetail);

        // When
        MemberDetailDto result = memberService.getMemberDetail(loginId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("테스트유저");
        assertThat(result.getLoginId()).isEqualTo(loginId);
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getRole()).isEqualTo(RoleType.USER);

        verify(memberRepository, times(1)).getMemberDetail(loginId);
    }

    // ==================== updateRole 테스트 ====================

    @DisplayName("유효한 권한과 로그인 아이디가 주어질 때 권한 변경을 하면 성공한다")
    @Test
    void givenValidRoleAndLoginId_whenUpdateRole_thenSuccess() {
        // Given
        String loginId = "test1234";
        String newRole = "ADMIN";

        Member member = Member.builder()
                .name("테스트유저")
                .email("test@test.com")
                .loginId(loginId)
                .password("encryptedPassword")
                .phoneNumber("010-1234-5678")
                .address("서울특별시 강남구")
                .role(RoleType.USER)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        when(memberRepository.findByLoginId(loginId)).thenReturn(Optional.of(member));

        // When
        memberService.updateRole(newRole, loginId);

        // Then
        assertThat(member.getRole()).isEqualTo(RoleType.ADMIN);

        verify(memberRepository, times(1)).findByLoginId(loginId);
    }

    @DisplayName("존재하지 않는 로그인 아이디가 주어질 때 권한 변경을 하면 예외가 발생한다")
    @Test
    void givenNonExistentLoginId_whenUpdateRole_thenThrowException() {
        // Given
        String loginId = "nonexistent";
        String newRole = "ADMIN";

        when(memberRepository.findByLoginId(loginId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberService.updateRole(newRole, loginId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.MEMBER_NOT_FOUND);

        verify(memberRepository, times(1)).findByLoginId(loginId);
    }

    // ==================== checkLoginCredentials 테스트 ====================

    @DisplayName("올바른 로그인 정보가 주어질 때 로그인 검증을 하면 성공한다")
    @Test
    void givenValidCredentials_whenCheckLoginCredentials_thenSuccess() {
        // Given
        String loginId = "test1234";
        String password = "password123!@#";

        Member member = Member.builder()
                .name("테스트유저")
                .email("test@test.com")
                .loginId(loginId)
                .password("encryptedPassword")
                .phoneNumber("010-1234-5678")
                .address("서울특별시 강남구")
                .role(RoleType.USER)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        when(memberRepository.findByLoginId(loginId)).thenReturn(Optional.of(member));
        doNothing().when(authUtils).checkPassword(member.getPassword(), password);

        // When
        JWTUserInfo result = memberService.checkLoginCredentials(loginId, password);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLoginId()).isEqualTo(loginId);
        assertThat(result.getRole()).isEqualTo(RoleType.USER);

        verify(memberRepository, times(1)).findByLoginId(loginId);
        verify(authUtils, times(1)).checkPassword(member.getPassword(), password);
    }

    @DisplayName("존재하지 않는 로그인 아이디가 주어질 때 로그인 검증을 하면 예외가 발생한다")
    @Test
    void givenNonExistentLoginId_whenCheckLoginCredentials_thenThrowException() {
        // Given
        String loginId = "nonexistent";
        String password = "password123!@#";

        when(memberRepository.findByLoginId(loginId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberService.checkLoginCredentials(loginId, password))
                .isInstanceOf(AuthenticationException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.MEMBER_NOT_FOUND);

        verify(memberRepository, times(1)).findByLoginId(loginId);
        verify(authUtils, never()).checkPassword(anyString(), anyString());
    }

    @DisplayName("잘못된 비밀번호가 주어질 때 로그인 검증을 하면 예외가 발생한다")
    @Test
    void givenInvalidPassword_whenCheckLoginCredentials_thenThrowException() {
        // Given
        String loginId = "test1234";
        String wrongPassword = "wrongPassword";

        Member member = Member.builder()
                .name("테스트유저")
                .email("test@test.com")
                .loginId(loginId)
                .password("encryptedPassword")
                .phoneNumber("010-1234-5678")
                .address("서울특별시 강남구")
                .role(RoleType.USER)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        when(memberRepository.findByLoginId(loginId)).thenReturn(Optional.of(member));
        doThrow(new BadCredentialsException("Password mismatch"))
                .when(authUtils).checkPassword(member.getPassword(), wrongPassword);

        // When & Then
        assertThatThrownBy(() -> memberService.checkLoginCredentials(loginId, wrongPassword))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Password mismatch");

        verify(memberRepository, times(1)).findByLoginId(loginId);
        verify(authUtils, times(1)).checkPassword(member.getPassword(), wrongPassword);
    }

    // ==================== checkDuplicateLoginId 테스트 ====================

    @DisplayName("사용 가능한 로그인 아이디가 주어질 때 중복 체크를 하면 성공한다")
    @Test
    void givenAvailableLoginId_whenCheckDuplicateLoginId_thenSuccess() {
        // Given
        String loginId = "newuser123";
        when(memberRepository.existsByLoginId(loginId)).thenReturn(false);

        // When
        memberService.checkDuplicateLoginId(loginId);

        // Then
        verify(memberRepository, times(1)).existsByLoginId(loginId);
    }

    @DisplayName("중복된 로그인 아이디가 주어질 때 중복 체크를 하면 예외가 발생한다")
    @Test
    void givenDuplicateLoginId_whenCheckDuplicateLoginId_thenThrowException() {
        // Given
        String loginId = "duplicate";
        when(memberRepository.existsByLoginId(loginId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> memberService.checkDuplicateLoginId(loginId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.VALID_LOGINID_DUPLICATE);

        verify(memberRepository, times(1)).existsByLoginId(loginId);
    }

    // ==================== checkDuplicateEmail 테스트 ====================

    @DisplayName("사용 가능한 이메일이 주어질 때 중복 체크를 하면 성공한다")
    @Test
    void givenAvailableEmail_whenCheckDuplicateEmail_thenSuccess() {
        // Given
        String email = "newuser@test.com";
        when(memberRepository.existsByEmail(email)).thenReturn(false);

        // When
        memberService.checkDuplicateEmail(email);

        // Then
        verify(memberRepository, times(1)).existsByEmail(email);
    }

    @DisplayName("중복된 이메일이 주어질 때 중복 체크를 하면 예외가 발생한다")
    @Test
    void givenDuplicateEmail_whenCheckDuplicateEmail_thenThrowException() {
        // Given
        String email = "duplicate@test.com";
        when(memberRepository.existsByEmail(email)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> memberService.checkDuplicateEmail(email))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.VALID_EMAIL_DUPLICATE);

        verify(memberRepository, times(1)).existsByEmail(email);
    }

    // ==================== checkDuplicatePhoneNumber 테스트 ====================

    @DisplayName("사용 가능한 전화번호가 주어질 때 중복 체크를 하면 성공한다")
    @Test
    void givenAvailablePhoneNumber_whenCheckDuplicatePhoneNumber_thenSuccess() {
        // Given
        String phoneNumber = "010-9999-8888";
        when(memberRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);

        // When
        memberService.checkDuplicatePhoneNumber(phoneNumber);

        // Then
        verify(memberRepository, times(1)).existsByPhoneNumber(phoneNumber);
    }

    @DisplayName("중복된 전화번호가 주어질 때 중복 체크를 하면 예외가 발생한다")
    @Test
    void givenDuplicatePhoneNumber_whenCheckDuplicatePhoneNumber_thenThrowException() {
        // Given
        String phoneNumber = "010-1234-5678";
        when(memberRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> memberService.checkDuplicatePhoneNumber(phoneNumber))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.VALID_PHONENUMBER_DUPLICATE);

        verify(memberRepository, times(1)).existsByPhoneNumber(phoneNumber);
    }

    // ==================== findMemberById 테스트 ====================

    @DisplayName("존재하는 회원 ID가 주어질 때 조회를 하면 성공한다")
    @Test
    void givenExistingMemberId_whenFindMemberById_thenSuccess() {
        // Given
        Long memberId = 1L;
        Member member = Member.builder()
                .name("테스트유저")
                .email("test@test.com")
                .loginId("test1234")
                .password("encryptedPassword")
                .phoneNumber("010-1234-5678")
                .address("서울특별시 강남구")
                .role(RoleType.USER)
                .build();
        ReflectionTestUtils.setField(member, "id", memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // When
        Member result = memberService.findMemberById(memberId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(memberId);
        assertThat(result.getName()).isEqualTo("테스트유저");
        assertThat(result.getLoginId()).isEqualTo("test1234");

        verify(memberRepository, times(1)).findById(memberId);
    }

    @DisplayName("존재하지 않는 회원 ID가 주어질 때 조회를 하면 예외가 발생한다")
    @Test
    void givenNonExistentMemberId_whenFindMemberById_thenThrowException() {
        // Given
        Long memberId = 999L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberService.findMemberById(memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.MEMBER_NOT_FOUND);

        verify(memberRepository, times(1)).findById(memberId);
    }

    // ==================== findMemberByLoginId 테스트 ====================

    @DisplayName("존재하는 로그인 아이디가 주어질 때 조회를 하면 성공한다")
    @Test
    void givenExistingLoginId_whenFindMemberByLoginId_thenSuccess() {
        // Given
        String loginId = "test1234";
        Member member = Member.builder()
                .name("테스트유저")
                .email("test@test.com")
                .loginId(loginId)
                .password("encryptedPassword")
                .phoneNumber("010-1234-5678")
                .address("서울특별시 강남구")
                .role(RoleType.USER)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        when(memberRepository.findByLoginId(loginId)).thenReturn(Optional.of(member));

        // When
        Member result = memberService.findMemberByLoginId(loginId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLoginId()).isEqualTo(loginId);
        assertThat(result.getName()).isEqualTo("테스트유저");

        verify(memberRepository, times(1)).findByLoginId(loginId);
    }

    @DisplayName("존재하지 않는 로그인 아이디가 주어질 때 조회를 하면 예외가 발생한다")
    @Test
    void givenNonExistentLoginId_whenFindMemberByLoginId_thenThrowException() {
        // Given
        String loginId = "nonexistent";
        when(memberRepository.findByLoginId(loginId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberService.findMemberByLoginId(loginId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.MEMBER_NOT_FOUND);

        verify(memberRepository, times(1)).findByLoginId(loginId);
    }
}
