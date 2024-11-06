package com.cookyuu.ecms_server.domain.coupon.facade;

import com.cookyuu.ecms_server.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponFacade {
    private final CouponRepository couponRepository;

}
