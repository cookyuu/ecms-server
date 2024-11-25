package com.cookyuu.ecms_server.domain.coupon.entity;

import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.entity.BaseTimeEntity;
import com.cookyuu.ecms_server.global.exception.domain.ECMSCouponException;
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
@Table(name = "ecms_coupon")
public class Coupon extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalDateTime startAt;
    private LocalDateTime expiredAt;
    private boolean isExpired;
    private CouponCode couponCode;
    private Integer discountPrice;
    private Integer quantity;
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
            throw new ECMSCouponException(ResultCode.COUPON_SOLD_OUT);
        }
        this.quantity = count;
    }

    public void issueFail(int count) {
        this.quantity = count;
    }
}
