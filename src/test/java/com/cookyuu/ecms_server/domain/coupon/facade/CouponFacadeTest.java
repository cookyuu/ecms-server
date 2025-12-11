package com.cookyuu.ecms_server.domain.coupon.facade;

import com.cookyuu.ecms_server.common.enums.RedisKeyCode;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.utils.RedisUtils;
import com.cookyuu.ecms_server.common.utils.RedissonUtils;
import com.cookyuu.ecms_server.domain.coupon.entity.Coupon;
import com.cookyuu.ecms_server.domain.coupon.enums.CouponCode;
import com.cookyuu.ecms_server.domain.coupon.logging.CouponLogHelper;
import com.cookyuu.ecms_server.domain.coupon.service.CouponService;
import com.cookyuu.ecms_server.domain.coupon.service.IssueCouponService;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponFacadeTest {

    @Spy
    @InjectMocks
    private CouponFacade couponFacade;

    @Mock
    private CouponService couponService;

    @Mock
    private MemberService memberService;

    @Mock
    private IssueCouponService issueCouponService;

    @Mock
    private RedisTemplate redisTemplate;

    @Mock
    private ValueOperations valueOperations;

    @Mock
    private RedisUtils redisUtils;

    @Mock
    private RedissonUtils redissonUtils;

    @Mock
    private CouponLogHelper couponLogHelper;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @DisplayName("유효한 쿠폰 발급 요청이 주어질 때 쿠폰을 발급하면 성공한다")
    @Test
    void givenValidIssueCouponRequest_whenIssueCoupon_thenSuccess() {
        // Given
        Long memberId = 1L;
        String couponNumber = "CPM240101000012345";

        doNothing().when(couponService).validateCoupon(couponNumber);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber)).thenReturn("10");
        when(redissonUtils.isIssuedCoupon(String.valueOf(memberId), couponNumber)).thenReturn(false);
        doNothing().when(redissonUtils).issueCoupon(String.valueOf(memberId), couponNumber);
        doNothing().when(couponFacade).processActualCouponIssue(memberId, couponNumber);
        doNothing().when(redissonUtils).recordIssueCouponStatus(couponNumber, "success");

        // When
        couponFacade.issueCoupon(memberId, couponNumber);

        // Then
        verify(couponService, times(1)).validateCoupon(couponNumber);
        verify(redissonUtils, times(1)).isIssuedCoupon(String.valueOf(memberId), couponNumber);
        verify(redissonUtils, times(1)).issueCoupon(String.valueOf(memberId), couponNumber);
        verify(couponFacade, times(1)).processActualCouponIssue(memberId, couponNumber);
        verify(redissonUtils, times(1)).recordIssueCouponStatus(couponNumber, "success");
    }

    @DisplayName("만료된 쿠폰이 주어질 때 쿠폰을 발급하면 예외가 발생한다")
    @Test
    void givenExpiredCoupon_whenIssueCoupon_thenThrowException() {
        // Given
        Long memberId = 1L;
        String couponNumber = "CPM240101000012345";

        doThrow(new BusinessException(ResultCode.COUPON_UNUSABLE))
                .when(couponService).validateCoupon(couponNumber);

        // When & Then
        assertThatThrownBy(() -> couponFacade.issueCoupon(memberId, couponNumber))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.BAD_REQUEST);

        verify(redissonUtils, times(1)).recordIssueCouponStatus(couponNumber, "failure");
    }

    @DisplayName("이미 발급된 쿠폰이 주어질 때 쿠폰을 발급하면 예외가 발생한다")
    @Test
    void givenAlreadyIssuedCoupon_whenIssueCoupon_thenThrowException() {
        // Given
        Long memberId = 1L;
        String couponNumber = "CPM240101000012345";

        doNothing().when(couponService).validateCoupon(couponNumber);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber)).thenReturn("10");
        when(redissonUtils.isIssuedCoupon(String.valueOf(memberId), couponNumber)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> couponFacade.issueCoupon(memberId, couponNumber))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.BAD_REQUEST);

        verify(redissonUtils, times(1)).recordIssueCouponStatus(couponNumber, "failure");
    }

    @DisplayName("쿠폰 수량이 0일 때 쿠폰을 발급하면 예외가 발생한다")
    @Test
    void givenSoldOutCoupon_whenIssueCoupon_thenThrowException() {
        // Given
        Long memberId = 1L;
        String couponNumber = "CPM240101000012345";

        doNothing().when(couponService).validateCoupon(couponNumber);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber)).thenReturn("0");

        // When & Then
        assertThatThrownBy(() -> couponFacade.issueCoupon(memberId, couponNumber))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.BAD_REQUEST);

        verify(redissonUtils, times(1)).recordIssueCouponStatus(couponNumber, "failure");
    }

    @DisplayName("쿠폰 수량이 null일 때 쿠폰을 발급하면 예외가 발생한다")
    @Test
    void givenNullCouponCount_whenIssueCoupon_thenThrowException() {
        // Given
        Long memberId = 1L;
        String couponNumber = "CPM240101000012345";

        doNothing().when(couponService).validateCoupon(couponNumber);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> couponFacade.issueCoupon(memberId, couponNumber))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.BAD_REQUEST);

        verify(redissonUtils, times(1)).recordIssueCouponStatus(couponNumber, "failure");
    }

    @DisplayName("쿠폰 발급 중 예외가 발생할 때 롤백하고 예외가 발생한다")
    @Test
    void givenExceptionDuringIssue_whenIssueCoupon_thenRollbackAndThrowException() {
        // Given
        Long memberId = 1L;
        String couponNumber = "CPM240101000012345";

        doNothing().when(couponService).validateCoupon(couponNumber);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber)).thenReturn("10");
        when(redissonUtils.isIssuedCoupon(String.valueOf(memberId), couponNumber)).thenReturn(false);
        doNothing().when(redissonUtils).issueCoupon(String.valueOf(memberId), couponNumber);
        doThrow(new BusinessException(ResultCode.COUPON_ISSUE_FAIL))
                .when(couponFacade).processActualCouponIssue(memberId, couponNumber);

        // When & Then
        assertThatThrownBy(() -> couponFacade.issueCoupon(memberId, couponNumber))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.BAD_REQUEST);

        verify(redissonUtils, times(1)).recordIssueCouponStatus(couponNumber, "failure");
    }

    @DisplayName("유효한 요청이 주어질 때 실제 쿠폰을 발급하면 성공한다")
    @Test
    void givenValidRequest_whenProcessActualCouponIssue_thenSuccess() {
        // Given
        Long memberId = 1L;
        String couponNumber = "CPM240101000012345";

        Member member = createMember(memberId, "testUser", "test@test.com");
        Coupon coupon = createCoupon(1L, couponNumber, 100);

        when(couponService.findCouponByCouponNumber(couponNumber)).thenReturn(coupon);
        when(memberService.findMemberById(memberId)).thenReturn(member);
        doNothing().when(issueCouponService).issueCoupon(member, coupon);
        when(redisUtils.getData(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber)).thenReturn("100");
        doNothing().when(couponService).save(coupon);

        // When
        couponFacade.processActualCouponIssue(memberId, couponNumber);

        // Then
        verify(couponService, times(1)).findCouponByCouponNumber(couponNumber);
        verify(memberService, times(1)).findMemberById(memberId);
        verify(issueCouponService, times(1)).issueCoupon(member, coupon);
        verify(couponService, times(1)).save(coupon);
    }

    @DisplayName("쿠폰 발급 중 예외가 발생할 때 롤백을 수행한다")
    @Test
    void givenExceptionDuringActualIssue_whenProcessActualCouponIssue_thenRollback() {
        // Given
        Long memberId = 1L;
        String couponNumber = "CPM240101000012345";

        Member member = createMember(memberId, "testUser", "test@test.com");
        Coupon coupon = createCoupon(1L, couponNumber, 100);

        when(couponService.findCouponByCouponNumber(couponNumber)).thenReturn(coupon);
        when(memberService.findMemberById(memberId)).thenReturn(member);
        doThrow(new RuntimeException("Database error"))
                .when(issueCouponService).issueCoupon(member, coupon);
        when(redisUtils.getData(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber)).thenReturn("100");
        doNothing().when(redissonUtils).issueCouponRollback(String.valueOf(memberId), couponNumber);
        doNothing().when(couponService).save(coupon);

        // When & Then
        assertThatThrownBy(() -> couponFacade.processActualCouponIssue(memberId, couponNumber))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(redissonUtils, times(1)).issueCouponRollback(String.valueOf(memberId), couponNumber);
        verify(couponService, times(1)).save(coupon);
    }

    @DisplayName("Redis 카운트 조회 실패 시 쿠폰 발급 중 예외가 발생한다")
    @Test
    void givenRedisCountError_whenProcessActualCouponIssue_thenThrowException() {
        // Given
        Long memberId = 1L;
        String couponNumber = "CPM240101000012345";

        Member member = createMember(memberId, "testUser", "test@test.com");
        Coupon coupon = createCoupon(1L, couponNumber, 100);

        when(couponService.findCouponByCouponNumber(couponNumber)).thenReturn(coupon);
        when(memberService.findMemberById(memberId)).thenReturn(member);
        doNothing().when(issueCouponService).issueCoupon(member, coupon);
        when(redisUtils.getData(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber))
                .thenThrow(new RuntimeException("Redis connection error"));
        doNothing().when(redissonUtils).issueCouponRollback(String.valueOf(memberId), couponNumber);
        doNothing().when(couponService).save(coupon);

        // When & Then
        assertThatThrownBy(() -> couponFacade.processActualCouponIssue(memberId, couponNumber))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Redis connection error");

        verify(redissonUtils, times(1)).issueCouponRollback(String.valueOf(memberId), couponNumber);
        verify(couponService, times(1)).save(coupon);
    }

    @DisplayName("쿠폰 발급 중 NumberFormatException 발생 시 롤백을 수행한다")
    @Test
    void givenInvalidCountFormat_whenProcessActualCouponIssue_thenRollback() {
        // Given
        Long memberId = 1L;
        String couponNumber = "CPM240101000012345";

        Member member = createMember(memberId, "testUser", "test@test.com");
        Coupon coupon = createCoupon(1L, couponNumber, 100);

        when(couponService.findCouponByCouponNumber(couponNumber)).thenReturn(coupon);
        when(memberService.findMemberById(memberId)).thenReturn(member);
        doNothing().when(issueCouponService).issueCoupon(member, coupon);
        when(redisUtils.getData(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber)).thenReturn("invalid");
        doNothing().when(redissonUtils).issueCouponRollback(String.valueOf(memberId), couponNumber);
        doNothing().when(couponService).save(coupon);

        // When & Then
        assertThatThrownBy(() -> couponFacade.processActualCouponIssue(memberId, couponNumber))
                .isInstanceOf(NumberFormatException.class);

        verify(redissonUtils, times(1)).issueCouponRollback(String.valueOf(memberId), couponNumber);
        verify(couponService, times(1)).save(coupon);
    }

    // Helper methods
    private Member createMember(Long id, String loginId, String email) {
        Member member = Member.builder()
                .loginId(loginId)
                .password("encodedPassword")
                .email(email)
                .name("테스트 회원")
                .phoneNumber("010-1234-5678")
                .role(RoleType.USER)
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private Coupon createCoupon(Long id, String couponNumber, Integer quantity) {
        Coupon coupon = Coupon.builder()
                .name("테스트 쿠폰")
                .startAt(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
                .expiredAt(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
                .couponCode(CouponCode.FIX_PRICE_DISCOUNT)
                .couponNumber(couponNumber)
                .quantity(quantity)
                .discountPrice(5000)
                .build();
        ReflectionTestUtils.setField(coupon, "id", id);
        return coupon;
    }
}
