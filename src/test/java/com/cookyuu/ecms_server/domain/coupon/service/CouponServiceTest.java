package com.cookyuu.ecms_server.domain.coupon.service;

import com.cookyuu.ecms_server.domain.coupon.dto.CreateCouponDto;
import com.cookyuu.ecms_server.domain.coupon.entity.Coupon;
import com.cookyuu.ecms_server.domain.coupon.enums.CouponCode;
import com.cookyuu.ecms_server.domain.coupon.repository.CouponRepository;
import com.cookyuu.ecms_server.common.enums.RedisKeyCode;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.generator.BusinessNumberGenerator;
import com.cookyuu.ecms_server.common.utils.RedisUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @InjectMocks
    private CouponService couponService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private RedisTemplate redisTemplate;

    @Mock
    private RedisUtils redisUtils;

    @Mock
    private BusinessNumberGenerator businessNumberGenerator;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @DisplayName("고정 금액 할인 쿠폰 정보가 주어질 때 쿠폰을 생성하면 성공한다")
    @Test
    void givenFixPriceCouponInfo_whenCreateCoupon_thenSuccess() {
        // Given
        CreateCouponDto.Request request = new CreateCouponDto.Request(
                "5000원 할인 쿠폰",
                "2024-01-01 00:00",
                "2024-12-31 23:59",
                "M",
                100,
                5000
        );

        Coupon savedCoupon = Coupon.builder()
                .name(request.getName())
                .startAt(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
                .expiredAt(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
                .couponCode(CouponCode.FIX_PRICE_DISCOUNT)
                .couponNumber("CPM240101000012345")
                .quantity(100)
                .discountPrice(5000)
                .build();
        ReflectionTestUtils.setField(savedCoupon, "id", 1L);

        when(businessNumberGenerator.generateCouponNumber(CouponCode.FIX_PRICE_DISCOUNT)).thenReturn("CPM240101000012345");
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);
        doNothing().when(redisUtils).setData(anyString(), anyString());

        // When
        CreateCouponDto.Response response = couponService.createCoupon(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCouponNumber()).isNotNull();
        assertThat(response.getCouponNumber()).startsWith("CPM");

        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository, times(1)).save(couponCaptor.capture());
        Coupon capturedCoupon = couponCaptor.getValue();
        assertThat(capturedCoupon.getName()).isEqualTo(request.getName());
        assertThat(capturedCoupon.getDiscountPrice()).isEqualTo(5000);
        assertThat(capturedCoupon.getCouponCode()).isEqualTo(CouponCode.FIX_PRICE_DISCOUNT);

        verify(redisUtils, times(1)).setData(anyString(), eq("100"));
    }

    @DisplayName("퍼센트 할인 쿠폰 정보가 주어질 때 쿠폰을 생성하면 성공한다")
    @Test
    void givenPercentageCouponInfo_whenCreateCoupon_thenSuccess() {
        // Given
        CreateCouponDto.Request request = new CreateCouponDto.Request(
                "10% 할인 쿠폰",
                "2024-01-01 00:00",
                "2024-12-31 23:59",
                "C",
                50,
                null
        );

        Coupon savedCoupon = Coupon.builder()
                .name(request.getName())
                .startAt(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
                .expiredAt(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
                .couponCode(CouponCode.TEN__PERCENT_DISCOUNT)
                .couponNumber("CPC240101000012345")
                .quantity(50)
                .discountPrice(null)
                .build();
        ReflectionTestUtils.setField(savedCoupon, "id", 1L);

        when(businessNumberGenerator.generateCouponNumber(CouponCode.TEN__PERCENT_DISCOUNT)).thenReturn("CPC240101000012345");
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);
        doNothing().when(redisUtils).setData(anyString(), anyString());

        // When
        CreateCouponDto.Response response = couponService.createCoupon(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCouponNumber()).isNotNull();
        assertThat(response.getCouponNumber()).startsWith("CPC");

        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository, times(1)).save(couponCaptor.capture());
        Coupon capturedCoupon = couponCaptor.getValue();
        assertThat(capturedCoupon.getName()).isEqualTo(request.getName());
        assertThat(capturedCoupon.getDiscountPrice()).isNull();
        assertThat(capturedCoupon.getCouponCode()).isEqualTo(CouponCode.TEN__PERCENT_DISCOUNT);

        verify(redisUtils, times(1)).setData(anyString(), eq("50"));
    }

    @DisplayName("고정 금액 쿠폰에서 할인 금액이 null일 때 쿠폰을 생성하면 예외가 발생한다")
    @Test
    void givenFixPriceCouponWithNullPrice_whenCreateCoupon_thenThrowException() {
        // Given
        CreateCouponDto.Request request = new CreateCouponDto.Request(
                "할인 쿠폰",
                "2024-01-01 00:00",
                "2024-12-31 23:59",
                "M",
                100,
                null
        );

        when(businessNumberGenerator.generateCouponNumber(CouponCode.FIX_PRICE_DISCOUNT)).thenReturn("CPM240101000012345");

        // When & Then
        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.COUPON_PRICE_EMPTY);
    }

    @DisplayName("고정 금액 쿠폰에서 할인 금액이 0일 때 쿠폰을 생성하면 예외가 발생한다")
    @Test
    void givenFixPriceCouponWithZeroPrice_whenCreateCoupon_thenThrowException() {
        // Given
        CreateCouponDto.Request request = new CreateCouponDto.Request(
                "할인 쿠폰",
                "2024-01-01 00:00",
                "2024-12-31 23:59",
                "M",
                100,
                0
        );

        when(businessNumberGenerator.generateCouponNumber(CouponCode.FIX_PRICE_DISCOUNT)).thenReturn("CPM240101000012345");

        // When & Then
        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.COUPON_PRICE_EMPTY);
    }

    @DisplayName("유효한 쿠폰 번호가 주어질 때 쿠폰을 조회하면 성공한다")
    @Test
    void givenValidCouponNumber_whenFindCouponByCouponNumber_thenSuccess() {
        // Given
        String couponNumber = "CPM240101000012345";
        Coupon coupon = Coupon.builder()
                .name("5000원 할인 쿠폰")
                .startAt(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
                .expiredAt(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
                .couponCode(CouponCode.FIX_PRICE_DISCOUNT)
                .couponNumber(couponNumber)
                .quantity(100)
                .discountPrice(5000)
                .build();
        ReflectionTestUtils.setField(coupon, "id", 1L);

        when(couponRepository.findByCouponNumber(couponNumber)).thenReturn(Optional.of(coupon));

        // When
        Coupon result = couponService.findCouponByCouponNumber(couponNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCouponNumber()).isEqualTo(couponNumber);
        verify(couponRepository, times(1)).findByCouponNumber(couponNumber);
    }

    @DisplayName("존재하지 않는 쿠폰 번호가 주어질 때 쿠폰을 조회하면 예외가 발생한다")
    @Test
    void givenNonExistentCouponNumber_whenFindCouponByCouponNumber_thenThrowException() {
        // Given
        String couponNumber = "INVALID_COUPON_NUMBER";

        when(couponRepository.findByCouponNumber(couponNumber)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> couponService.findCouponByCouponNumber(couponNumber))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.COUPON_NOT_FOUND);
    }

    @DisplayName("유효한 쿠폰이 주어질 때 쿠폰을 검증하면 성공한다")
    @Test
    void givenValidCoupon_whenValidateCoupon_thenSuccess() {
        // Given
        String couponNumber = "CPM240101000012345";
        Coupon coupon = Coupon.builder()
                .name("5000원 할인 쿠폰")
                .startAt(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
                .expiredAt(LocalDateTime.now().plusDays(30))
                .couponCode(CouponCode.FIX_PRICE_DISCOUNT)
                .couponNumber(couponNumber)
                .quantity(100)
                .discountPrice(5000)
                .build();
        ReflectionTestUtils.setField(coupon, "id", 1L);

        when(couponRepository.findByCouponNumber(couponNumber)).thenReturn(Optional.of(coupon));

        // When & Then
        // Should not throw exception
        couponService.validateCoupon(couponNumber);

        verify(couponRepository, times(1)).findByCouponNumber(couponNumber);
    }

    @DisplayName("만료된 쿠폰이 주어질 때 쿠폰을 검증하면 예외가 발생한다")
    @Test
    void givenExpiredCoupon_whenValidateCoupon_thenThrowException() {
        // Given
        String couponNumber = "CPM240101000012345";
        Coupon coupon = Coupon.builder()
                .name("5000원 할인 쿠폰")
                .startAt(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
                .expiredAt(LocalDateTime.now().minusDays(1))
                .couponCode(CouponCode.FIX_PRICE_DISCOUNT)
                .couponNumber(couponNumber)
                .quantity(100)
                .discountPrice(5000)
                .build();
        ReflectionTestUtils.setField(coupon, "id", 1L);

        when(couponRepository.findByCouponNumber(couponNumber)).thenReturn(Optional.of(coupon));

        // When & Then
        assertThatThrownBy(() -> couponService.validateCoupon(couponNumber))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.COUPON_UNUSABLE);
    }

    @DisplayName("수량이 0인 쿠폰이 주어질 때 쿠폰을 검증하면 예외가 발생한다")
    @Test
    void givenSoldOutCoupon_whenValidateCoupon_thenThrowException() {
        // Given
        String couponNumber = "CPM240101000012345";
        Coupon coupon = Coupon.builder()
                .name("5000원 할인 쿠폰")
                .startAt(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
                .expiredAt(LocalDateTime.now().plusDays(30))
                .couponCode(CouponCode.FIX_PRICE_DISCOUNT)
                .couponNumber(couponNumber)
                .quantity(0)
                .discountPrice(5000)
                .build();
        ReflectionTestUtils.setField(coupon, "id", 1L);

        when(couponRepository.findByCouponNumber(couponNumber)).thenReturn(Optional.of(coupon));

        // When & Then
        assertThatThrownBy(() -> couponService.validateCoupon(couponNumber))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.COUPON_SOLD_OUT);
    }

    @DisplayName("유효한 쿠폰이 주어질 때 저장하면 성공한다")
    @Test
    void givenValidCoupon_whenSave_thenSuccess() {
        // Given
        Coupon coupon = Coupon.builder()
                .name("5000원 할인 쿠폰")
                .startAt(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
                .expiredAt(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
                .couponCode(CouponCode.FIX_PRICE_DISCOUNT)
                .couponNumber("CPM240101000012345")
                .quantity(100)
                .discountPrice(5000)
                .build();
        ReflectionTestUtils.setField(coupon, "id", 1L);

        when(couponRepository.save(coupon)).thenReturn(coupon);

        // When
        couponService.save(coupon);

        // Then
        verify(couponRepository, times(1)).save(coupon);
    }
}
