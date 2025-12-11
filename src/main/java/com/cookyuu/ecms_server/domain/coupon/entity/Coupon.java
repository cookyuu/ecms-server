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

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private boolean isExpired = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CouponCode couponCode;

    @Column(nullable = false)
    private Integer discountPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, unique = true, length = 50)
    private String couponNumber;

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
