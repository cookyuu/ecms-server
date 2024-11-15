package com.cookyuu.ecms_server.domain.coupon.repository;

import com.cookyuu.ecms_server.domain.coupon.entity.IssueCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueCouponRepository extends JpaRepository<IssueCoupon, Long> {
}
