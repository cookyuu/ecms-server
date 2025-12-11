package com.cookyuu.ecms_server.domain.seller.service;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.AuthenticationException;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.utils.AuthUtils;
import com.cookyuu.ecms_server.common.utils.UserUtils;
import com.cookyuu.ecms_server.common.utils.ValidateUtils;
import com.cookyuu.ecms_server.domain.auth.dto.JWTUserInfo;
import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import com.cookyuu.ecms_server.domain.seller.dto.DeleteSellerDto;
import com.cookyuu.ecms_server.domain.seller.dto.RegisterSellerDto;
import com.cookyuu.ecms_server.domain.seller.dto.SellerDetailDto;
import com.cookyuu.ecms_server.domain.seller.dto.UpdateSellerDto;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import com.cookyuu.ecms_server.domain.seller.repository.SellerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @InjectMocks
    private SellerService sellerService;

    @Mock
    private SellerRepository sellerRepository;

    @Spy
    private ValidateUtils validateUtils;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private UserUtils userUtils;

    @BeforeEach
    void setUp() {
        // Mock userUtils.getUserId to return Long from user.getUsername()
        lenient().when(userUtils.getUserId(any(UserDetails.class)))
            .thenAnswer(invocation -> {
                UserDetails user = invocation.getArgument(0);
                return Long.parseLong(user.getUsername());
            });
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    // ==================== findSellerById 테스트 ====================

    @DisplayName("존재하는 판매자 ID가 주어질 때 조회를 하면 성공한다")
    @Test
    void givenExistingSellerId_whenFindSellerById_thenSuccess() {
        // Given
        Long sellerId = 1L;
        Seller seller = Seller.builder()
                .name("테스트 판매자")
                .loginId("seller123")
                .password("encryptedPassword")
                .businessName("테스트 비즈니스")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구")
                .businessContactTelNum("02-1234-5678")
                .businessContactEmail("seller@test.com")
                .role(RoleType.SELLER)
                .build();
        ReflectionTestUtils.setField(seller, "id", sellerId);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));

        // When
        Seller result = sellerService.findSellerById(sellerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(sellerId);
        assertThat(result.getName()).isEqualTo("테스트 판매자");
        assertThat(result.getLoginId()).isEqualTo("seller123");

        verify(sellerRepository, times(1)).findById(sellerId);
    }

    @DisplayName("존재하지 않는 판매자 ID가 주어질 때 조회를 하면 예외가 발생한다")
    @Test
    void givenNonExistentSellerId_whenFindSellerById_thenThrowException() {
        // Given
        Long sellerId = 999L;
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sellerService.findSellerById(sellerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.SELLER_NOT_FOUND);

        verify(sellerRepository, times(1)).findById(sellerId);
    }

    // ==================== registerSeller 테스트 ====================

    @DisplayName("유효한 판매자 정보가 주어질 때 판매자 등록을 하면 성공한다")
    @Test
    void givenValidSellerInfo_whenRegisterSeller_thenSuccess() {
        // Given
        RegisterSellerDto.Request sellerInfo = new RegisterSellerDto.Request(
                "seller123",
                "password123!@#",
                "테스트 판매자",
                "테스트 비즈니스",
                "101-81-00155",
                "서울시 강남구",
                "010-1234-5678",
                "seller@test.com"
        );

        String encryptedPassword = "encryptedPassword";
        Seller savedSeller = Seller.builder()
                .name(sellerInfo.getName())
                .loginId(sellerInfo.getLoginId())
                .password(encryptedPassword)
                .businessName(sellerInfo.getBusinessName())
                .businessNumber(sellerInfo.getBusinessNumber())
                .businessAddress(sellerInfo.getBusinessAddress())
                .businessContactTelNum(sellerInfo.getBusinessContactTelNum())
                .businessContactEmail(sellerInfo.getBusinessContactEmail())
                .role(RoleType.SELLER)
                .build();
        ReflectionTestUtils.setField(savedSeller, "id", 1L);

        when(sellerRepository.existsByLoginId(sellerInfo.getLoginId())).thenReturn(false);
        when(sellerRepository.existsByBusinessNumber(sellerInfo.getBusinessNumber())).thenReturn(false);
        doNothing().when(validateUtils).isAvailableBusinessNumber(sellerInfo.getBusinessNumber());
        doNothing().when(validateUtils).isAvailablePhoneNumberFormat(sellerInfo.getBusinessContactTelNum());
        doNothing().when(validateUtils).isAvailableEmailFormat(sellerInfo.getBusinessContactEmail());
        doNothing().when(validateUtils).isAvailablePasswordFormat(sellerInfo.getPassword());
        when(authUtils.encryptPassword(sellerInfo.getPassword())).thenReturn(encryptedPassword);
        when(sellerRepository.save(any(Seller.class))).thenReturn(savedSeller);

        // When
        RegisterSellerDto.Response result = sellerService.registerSeller(sellerInfo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSellerId()).isEqualTo(1L);

        verify(sellerRepository, times(1)).existsByLoginId(sellerInfo.getLoginId());
        verify(sellerRepository, times(1)).existsByBusinessNumber(sellerInfo.getBusinessNumber());
        verify(authUtils, times(1)).encryptPassword(sellerInfo.getPassword());
        verify(sellerRepository, times(1)).save(any(Seller.class));
    }

    @DisplayName("중복된 로그인 아이디가 주어질 때 판매자 등록을 하면 예외가 발생한다")
    @Test
    void givenDuplicateLoginId_whenRegisterSeller_thenThrowException() {
        // Given
        RegisterSellerDto.Request sellerInfo = new RegisterSellerDto.Request(
                "duplicate",
                "password123!@#",
                "테스트 판매자",
                "테스트 비즈니스",
                "101-81-00155",
                "서울시 강남구",
                "010-1234-5678",
                "seller@test.com"
        );

        when(sellerRepository.existsByLoginId(sellerInfo.getLoginId())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> sellerService.registerSeller(sellerInfo))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.VALID_LOGINID_DUPLICATE);

        verify(sellerRepository, times(1)).existsByLoginId(sellerInfo.getLoginId());
        verify(sellerRepository, never()).save(any(Seller.class));
    }

    @DisplayName("중복된 사업자 번호가 주어질 때 판매자 등록을 하면 예외가 발생한다")
    @Test
    void givenDuplicateBusinessNumber_whenRegisterSeller_thenThrowException() {
        // Given
        RegisterSellerDto.Request sellerInfo = new RegisterSellerDto.Request(
                "seller123",
                "password123!@#",
                "테스트 판매자",
                "테스트 비즈니스",
                "101-81-00155",
                "서울시 강남구",
                "010-1234-5678",
                "seller@test.com"
        );

        when(sellerRepository.existsByLoginId(sellerInfo.getLoginId())).thenReturn(false);
        doNothing().when(validateUtils).isAvailableBusinessNumber(sellerInfo.getBusinessNumber());
        when(sellerRepository.existsByBusinessNumber(sellerInfo.getBusinessNumber())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> sellerService.registerSeller(sellerInfo))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.VALID_BUSINESSNUM_DUPLICATE);

        verify(sellerRepository, times(1)).existsByLoginId(sellerInfo.getLoginId());
        verify(sellerRepository, times(1)).existsByBusinessNumber(sellerInfo.getBusinessNumber());
        verify(sellerRepository, never()).save(any(Seller.class));
    }

    @DisplayName("잘못된 비밀번호 형식이 주어질 때 판매자 등록을 하면 예외가 발생한다")
    @Test
    void givenInvalidPasswordFormat_whenRegisterSeller_thenThrowException() {
        // Given
        RegisterSellerDto.Request sellerInfo = new RegisterSellerDto.Request(
                "seller123",
                "weakpw",  // 약한 비밀번호
                "테스트 판매자",
                "테스트 비즈니스",
                "101-81-00155",
                "서울시 강남구",
                "010-1234-5678",
                "seller@test.com"
        );

        when(sellerRepository.existsByLoginId(sellerInfo.getLoginId())).thenReturn(false);
        doNothing().when(validateUtils).isAvailableBusinessNumber(sellerInfo.getBusinessNumber());
        when(sellerRepository.existsByBusinessNumber(sellerInfo.getBusinessNumber())).thenReturn(false);
        // Don't mock password validation to let it throw the exception

        // When & Then
        assertThatThrownBy(() -> sellerService.registerSeller(sellerInfo))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.VALID_PASSWORD_FORMAT);

        verify(sellerRepository, never()).save(any(Seller.class));
    }

    // ==================== updateSellerInfo 테스트 ====================

    @DisplayName("유효한 수정 정보가 주어질 때 판매자 정보 수정을 하면 성공한다")
    @Test
    void givenValidUpdateInfo_whenUpdateSellerInfo_thenSuccess() {
        // Given
        UserDetails user = User.builder()
                .username("1")
                .password("encryptedPassword")
                .authorities(Collections.emptyList())
                .build();

        UpdateSellerDto.Request updateInfo = new UpdateSellerDto.Request(
                "password123!@#",
                "수정된 이름",
                "수정된 비즈니스",
                "수정된 주소",
                "010-9999-8888",
                "updated@test.com"
        );

        Seller seller = Seller.builder()
                .name("기존 이름")
                .loginId("seller123")
                .password("encryptedPassword")
                .businessName("기존 비즈니스")
                .businessNumber("123-45-67890")
                .businessAddress("기존 주소")
                .businessContactTelNum("02-1234-5678")
                .businessContactEmail("seller@test.com")
                .role(RoleType.SELLER)
                .build();
        ReflectionTestUtils.setField(seller, "id", 1L);

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        doNothing().when(authUtils).checkPassword("encryptedPassword", "password123!@#");

        // When
        sellerService.updateSellerInfo(user, updateInfo);

        // Then
        verify(sellerRepository, times(1)).findById(1L);
        verify(authUtils, times(1)).checkPassword("encryptedPassword", "password123!@#");
    }

    @DisplayName("모든 필드가 null인 경우 판매자 정보 수정을 하면 예외가 발생한다")
    @Test
    void givenAllNullFields_whenUpdateSellerInfo_thenThrowException() {
        // Given
        UserDetails user = User.builder()
                .username("1")
                .password("encryptedPassword")
                .authorities(Collections.emptyList())
                .build();

        UpdateSellerDto.Request updateInfo = new UpdateSellerDto.Request(
                "password123!@#",
                null, null, null, null, null
        );

        // When & Then
        assertThatThrownBy(() -> sellerService.updateSellerInfo(user, updateInfo))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.REQUEST_DATA_ISNULL);

        verify(sellerRepository, never()).findById(anyLong());
    }

    @DisplayName("잘못된 비밀번호가 주어질 때 판매자 정보 수정을 하면 예외가 발생한다")
    @Test
    void givenWrongPassword_whenUpdateSellerInfo_thenThrowException() {
        // Given
        UserDetails user = User.builder()
                .username("1")
                .password("encryptedPassword")
                .authorities(Collections.emptyList())
                .build();

        UpdateSellerDto.Request updateInfo = new UpdateSellerDto.Request(
                "wrongPassword",
                "수정된 이름",
                null, null, null, null
        );

        Seller seller = Seller.builder()
                .name("기존 이름")
                .loginId("seller123")
                .password("encryptedPassword")
                .businessName("기존 비즈니스")
                .businessNumber("123-45-67890")
                .businessAddress("기존 주소")
                .businessContactTelNum("02-1234-5678")
                .businessContactEmail("seller@test.com")
                .role(RoleType.SELLER)
                .build();
        ReflectionTestUtils.setField(seller, "id", 1L);

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        doThrow(new BadCredentialsException("Password mismatch"))
                .when(authUtils).checkPassword("encryptedPassword", "wrongPassword");

        // When & Then
        assertThatThrownBy(() -> sellerService.updateSellerInfo(user, updateInfo))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Password mismatch");

        verify(sellerRepository, times(1)).findById(1L);
        verify(authUtils, times(1)).checkPassword("encryptedPassword", "wrongPassword");
    }

    // ==================== deleteSeller 테스트 ====================

    @DisplayName("유효한 비밀번호가 주어질 때 판매자 삭제를 하면 성공한다")
    @Test
    void givenValidPassword_whenDeleteSeller_thenSuccess() {
        // Given
        UserDetails user = User.builder()
                .username("1")
                .password("encryptedPassword")
                .authorities(Collections.emptyList())
                .build();

        DeleteSellerDto.Request deleteInfo = new DeleteSellerDto.Request(
                "password123!@#",
                "password123!@#"
        );

        Seller seller = Seller.builder()
                .name("테스트 판매자")
                .loginId("seller123")
                .password("encryptedPassword")
                .businessName("테스트 비즈니스")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구")
                .businessContactTelNum("02-1234-5678")
                .businessContactEmail("seller@test.com")
                .role(RoleType.SELLER)
                .build();
        ReflectionTestUtils.setField(seller, "id", 1L);

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        doNothing().when(authUtils).checkPassword("encryptedPassword", "password123!@#");

        // When
        sellerService.deleteSeller(user, deleteInfo);

        // Then
        // Seller.delete() method is called
        verify(sellerRepository, times(1)).findById(1L);
        verify(authUtils, times(1)).checkPassword("encryptedPassword", "password123!@#");
    }

    @DisplayName("비밀번호 확인이 일치하지 않을 때 판매자 삭제를 하면 예외가 발생한다")
    @Test
    void givenMismatchedConfirmPassword_whenDeleteSeller_thenThrowException() {
        // Given
        UserDetails user = User.builder()
                .username("1")
                .password("encryptedPassword")
                .authorities(Collections.emptyList())
                .build();

        DeleteSellerDto.Request deleteInfo = new DeleteSellerDto.Request(
                "password123!@#",
                "differentPassword"
        );

        // When & Then
        assertThatThrownBy(() -> sellerService.deleteSeller(user, deleteInfo))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.CONFIRM_PASSWORD_UNMATCHED);

        verify(sellerRepository, never()).findById(anyLong());
    }

    @DisplayName("잘못된 비밀번호가 주어질 때 판매자 삭제를 하면 예외가 발생한다")
    @Test
    void givenWrongPassword_whenDeleteSeller_thenThrowException() {
        // Given
        UserDetails user = User.builder()
                .username("1")
                .password("encryptedPassword")
                .authorities(Collections.emptyList())
                .build();

        DeleteSellerDto.Request deleteInfo = new DeleteSellerDto.Request(
                "wrongPassword",
                "wrongPassword"
        );

        Seller seller = Seller.builder()
                .name("테스트 판매자")
                .loginId("seller123")
                .password("encryptedPassword")
                .businessName("테스트 비즈니스")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구")
                .businessContactTelNum("02-1234-5678")
                .businessContactEmail("seller@test.com")
                .role(RoleType.SELLER)
                .build();
        ReflectionTestUtils.setField(seller, "id", 1L);

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        doThrow(new BadCredentialsException("Password mismatch"))
                .when(authUtils).checkPassword("encryptedPassword", "wrongPassword");

        // When & Then
        assertThatThrownBy(() -> sellerService.deleteSeller(user, deleteInfo))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Password mismatch");

        verify(sellerRepository, times(1)).findById(1L);
        verify(authUtils, times(1)).checkPassword("encryptedPassword", "wrongPassword");
    }

    // ==================== getSellerDetail 테스트 ====================

    @DisplayName("판매자 ID가 주어질 때 판매자 상세 조회를 하면 성공한다")
    @Test
    void givenSellerId_whenGetSellerDetail_thenSuccess() {
        // Given
        Long sellerId = 1L;
        SellerDetailDto expectedDetail = new SellerDetailDto(
                sellerId,
                "테스트 판매자",
                "테스트 비즈니스",
                "101-81-00155",
                "서울시 강남구",
                "010-1234-5678",
                "seller@test.com",
                "seller123"
        );

        when(sellerRepository.getSellerDetail(sellerId)).thenReturn(expectedDetail);

        // When
        SellerDetailDto result = sellerService.getSellerDetail(sellerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSellerId()).isEqualTo(sellerId);
        assertThat(result.getName()).isEqualTo("테스트 판매자");
        assertThat(result.getLoginId()).isEqualTo("seller123");

        verify(sellerRepository, times(1)).getSellerDetail(sellerId);
    }

    // ==================== checkLoginCredentials 테스트 ====================

    @DisplayName("올바른 로그인 정보가 주어질 때 로그인 검증을 하면 성공한다")
    @Test
    void givenValidCredentials_whenCheckLoginCredentials_thenSuccess() {
        // Given
        String loginId = "seller123";
        String password = "password123!@#";

        Seller seller = Seller.builder()
                .name("테스트 판매자")
                .loginId(loginId)
                .password("encryptedPassword")
                .businessName("테스트 비즈니스")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구")
                .businessContactTelNum("02-1234-5678")
                .businessContactEmail("seller@test.com")
                .role(RoleType.SELLER)
                .build();
        ReflectionTestUtils.setField(seller, "id", 1L);

        when(sellerRepository.findByLoginId(loginId)).thenReturn(Optional.of(seller));
        doNothing().when(authUtils).checkPassword("encryptedPassword", password);

        // When
        JWTUserInfo result = sellerService.checkLoginCredentials(loginId, password);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLoginId()).isEqualTo(loginId);
        assertThat(result.getRole()).isEqualTo(RoleType.SELLER);

        verify(sellerRepository, times(1)).findByLoginId(loginId);
        verify(authUtils, times(1)).checkPassword("encryptedPassword", password);
    }

    @DisplayName("존재하지 않는 로그인 아이디가 주어질 때 로그인 검증을 하면 예외가 발생한다")
    @Test
    void givenNonExistentLoginId_whenCheckLoginCredentials_thenThrowException() {
        // Given
        String loginId = "nonexistent";
        String password = "password123!@#";

        when(sellerRepository.findByLoginId(loginId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sellerService.checkLoginCredentials(loginId, password))
                .isInstanceOf(AuthenticationException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.SELLER_NOT_FOUND);

        verify(sellerRepository, times(1)).findByLoginId(loginId);
        verify(authUtils, never()).checkPassword(anyString(), anyString());
    }

    @DisplayName("잘못된 비밀번호가 주어질 때 로그인 검증을 하면 예외가 발생한다")
    @Test
    void givenWrongPassword_whenCheckLoginCredentials_thenThrowException() {
        // Given
        String loginId = "seller123";
        String wrongPassword = "wrongPassword";

        Seller seller = Seller.builder()
                .name("테스트 판매자")
                .loginId(loginId)
                .password("encryptedPassword")
                .businessName("테스트 비즈니스")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구")
                .businessContactTelNum("02-1234-5678")
                .businessContactEmail("seller@test.com")
                .role(RoleType.SELLER)
                .build();
        ReflectionTestUtils.setField(seller, "id", 1L);

        when(sellerRepository.findByLoginId(loginId)).thenReturn(Optional.of(seller));
        doThrow(new BadCredentialsException("Password mismatch"))
                .when(authUtils).checkPassword("encryptedPassword", wrongPassword);

        // When & Then
        assertThatThrownBy(() -> sellerService.checkLoginCredentials(loginId, wrongPassword))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Password mismatch");

        verify(sellerRepository, times(1)).findByLoginId(loginId);
        verify(authUtils, times(1)).checkPassword("encryptedPassword", wrongPassword);
    }
}
