package com.cookyuu.ecms_server.domain.coupon.entity;

import com.cookyuu.ecms_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ecms_coupon")
public class Coupon extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Long stockQuantity;

    private Integer price;

    private String couponNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponCode couponCode;

    @Column(nullable = false)
    private LocalDate expiredAt;
}
