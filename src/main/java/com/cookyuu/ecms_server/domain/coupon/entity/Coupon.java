package com.cookyuu.ecms_server.domain.coupon.entity;

import com.cookyuu.ecms_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    @NotBlank
    private String name;
    @NotBlank
    private LocalDate expiredAt;
    private boolean isExpired;
    @NotBlank
    private CouponCode couponCode;
    private Integer discountPrice;
    @NotBlank
    private String couponNumber;

    @OneToMany(mappedBy = "coupon")
    private List<IssueCoupon> issueCoupons = new ArrayList<>();

    @Builder
    Coupon (String name, LocalDate expiredAt, CouponCode couponCode, Integer discountPrice, String couponNumber) {
        this.name = name;
        this.expiredAt = expiredAt;
        this.couponCode = couponCode;
        this.discountPrice = discountPrice;
        this.couponNumber = couponNumber;
    }
}
