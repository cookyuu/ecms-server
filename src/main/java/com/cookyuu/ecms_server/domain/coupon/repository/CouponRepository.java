package com.cookyuu.ecms_server.domain.coupon.repository;

import com.cookyuu.ecms_server.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCouponNumber(String couponNumber);
}
