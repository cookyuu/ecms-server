package com.cookyuu.ecms_server.domain.coupon.entity;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.domain.BaseTimeEntity;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.domain.coupon.enums.CouponCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 쿠폰 엔티티
 *
 * - 발급쿠폰(IssueCoupon) 1:N 관계
 */
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "ecms_coupon",
        indexes = {
                @Index(name = "ecms_coupon_search_idx_1", columnList = "couponNumber", unique = true),
                @Index(name = "ecms_coupon_search_idx_2", columnList = "couponCode")
        }
)
public class Coupon extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 쿠폰명
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 쿠폰 시작일
     */
    @Column(nullable = false)
    private LocalDateTime startAt;

    /**
     * 쿠폰 만료일
     */
    @Column(nullable = false)
    private LocalDateTime expiredAt;

    /**
     * 만료 여부
     */
    @Column(nullable = false)
    private boolean isExpired = false;

    /**
     * 쿠폰 코드
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CouponCode couponCode;

    /**
     * 할인 금액
     */
    @Column(nullable = false)
    private Integer discountPrice;

    /**
     * 남은 수량
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * 쿠폰 번호 (고유)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String couponNumber;

    /**
     * 발급 쿠폰 목록 (읽기 전용)
     */
    @OneToMany(mappedBy = "coupon")
    private List<IssueCoupon> issueCoupons = new ArrayList<>();

    @Builder
    Coupon (String name, LocalDateTime startAt, LocalDateTime expiredAt, CouponCode couponCode, Integer discountPrice, Integer quantity, String couponNumber) {
        this.name = name;
        this.startAt = startAt;
        this.expiredAt = expiredAt;
        this.couponCode = couponCode;
        this.discountPrice = discountPrice;
        this.quantity = quantity;
        this.couponNumber = couponNumber;
    }

    public void issue(int count) {
        if (this.quantity == 0) {
            throw new BusinessException(ResultCode.COUPON_SOLD_OUT);
        }
        this.quantity = count;
    }

    public void issueFail(int count) {
        this.quantity = count;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt) || this.isExpired;
    }
}
