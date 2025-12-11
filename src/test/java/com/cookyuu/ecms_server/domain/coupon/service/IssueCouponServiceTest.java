package com.cookyuu.ecms_server.domain.coupon.service;

import com.cookyuu.ecms_server.domain.coupon.entity.Coupon;
import com.cookyuu.ecms_server.domain.coupon.entity.IssueCoupon;
import com.cookyuu.ecms_server.domain.coupon.enums.CouponCode;
import com.cookyuu.ecms_server.domain.coupon.repository.IssueCouponRepository;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueCouponServiceTest {

    @InjectMocks
    private IssueCouponService issueCouponService;

    @Mock
    private IssueCouponRepository issueCouponRepository;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @DisplayName("유효한 회원과 쿠폰이 주어질 때 쿠폰을 발급하면 성공한다")
    @Test
    void givenValidMemberAndCoupon_whenIssueCoupon_thenSuccess() {
        // Given
        Member member = createMember(1L, "testUser", "test@test.com");
        Coupon coupon = createCoupon(1L, "CPM240101000012345", 100);

        IssueCoupon savedIssueCoupon = IssueCoupon.builder()
                .expiredAt(coupon.getExpiredAt())
                .coupon(coupon)
                .member(member)
                .build();
        ReflectionTestUtils.setField(savedIssueCoupon, "id", 1L);

        when(issueCouponRepository.save(any(IssueCoupon.class))).thenReturn(savedIssueCoupon);

        // When
        issueCouponService.issueCoupon(member, coupon);

        // Then
        ArgumentCaptor<IssueCoupon> issueCouponCaptor = ArgumentCaptor.forClass(IssueCoupon.class);
        verify(issueCouponRepository, times(1)).save(issueCouponCaptor.capture());

        IssueCoupon capturedIssueCoupon = issueCouponCaptor.getValue();
        assertThat(capturedIssueCoupon.getMember()).isEqualTo(member);
        assertThat(capturedIssueCoupon.getCoupon()).isEqualTo(coupon);
        assertThat(capturedIssueCoupon.getExpiredAt()).isEqualTo(coupon.getExpiredAt());
        assertThat(capturedIssueCoupon.isUseAble()).isTrue();
    }

    @DisplayName("다수의 회원에게 동일한 쿠폰을 발급할 때 각각 발급되면 성공한다")
    @Test
    void givenMultipleMembers_whenIssueSameCoupon_thenSuccess() {
        // Given
        Member member1 = createMember(1L, "user1", "user1@test.com");
        Member member2 = createMember(2L, "user2", "user2@test.com");
        Coupon coupon = createCoupon(1L, "CPM240101000012345", 100);

        when(issueCouponRepository.save(any(IssueCoupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        issueCouponService.issueCoupon(member1, coupon);
        issueCouponService.issueCoupon(member2, coupon);

        // Then
        verify(issueCouponRepository, times(2)).save(any(IssueCoupon.class));
    }

    @DisplayName("동일한 회원에게 다른 쿠폰을 발급할 때 각각 발급되면 성공한다")
    @Test
    void givenSameMember_whenIssueDifferentCoupons_thenSuccess() {
        // Given
        Member member = createMember(1L, "testUser", "test@test.com");
        Coupon coupon1 = createCoupon(1L, "CPM240101000012345", 100);
        Coupon coupon2 = createCoupon(2L, "CPM240102000067890", 50);

        when(issueCouponRepository.save(any(IssueCoupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        issueCouponService.issueCoupon(member, coupon1);
        issueCouponService.issueCoupon(member, coupon2);

        // Then
        verify(issueCouponRepository, times(2)).save(any(IssueCoupon.class));
    }

    @DisplayName("만료일이 다가오는 쿠폰을 발급할 때 쿠폰의 만료일이 설정되면 성공한다")
    @Test
    void givenCouponNearExpiry_whenIssueCoupon_thenSetExpiryDate() {
        // Given
        Member member = createMember(1L, "testUser", "test@test.com");
        LocalDateTime nearExpiryDate = LocalDateTime.now().plusDays(7);
        Coupon coupon = Coupon.builder()
                .name("곧 만료될 쿠폰")
                .startAt(LocalDateTime.now().minusDays(30))
                .expiredAt(nearExpiryDate)
                .couponCode(CouponCode.FIX_PRICE_DISCOUNT)
                .couponNumber("CPM240101000012345")
                .quantity(100)
                .discountPrice(5000)
                .build();
        ReflectionTestUtils.setField(coupon, "id", 1L);

        when(issueCouponRepository.save(any(IssueCoupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        issueCouponService.issueCoupon(member, coupon);

        // Then
        ArgumentCaptor<IssueCoupon> issueCouponCaptor = ArgumentCaptor.forClass(IssueCoupon.class);
        verify(issueCouponRepository, times(1)).save(issueCouponCaptor.capture());

        IssueCoupon capturedIssueCoupon = issueCouponCaptor.getValue();
        assertThat(capturedIssueCoupon.getExpiredAt()).isEqualTo(nearExpiryDate);
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
