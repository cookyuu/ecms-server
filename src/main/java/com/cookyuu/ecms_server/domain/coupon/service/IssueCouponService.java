package com.cookyuu.ecms_server.domain.coupon.service;

import com.cookyuu.ecms_server.domain.coupon.entity.Coupon;
import com.cookyuu.ecms_server.domain.coupon.entity.IssueCoupon;
import com.cookyuu.ecms_server.domain.coupon.repository.IssueCouponRepository;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueCouponService {
    private final IssueCouponRepository issueCouponRepository;

    @Transactional
    public void issueCoupon(Member member, Coupon coupon) {
        log.info("[Coupon::Issue] Insert issueCoupon");
        IssueCoupon issueCoupon = IssueCoupon.builder()
                .expiredAt(coupon.getExpiredAt())
                .coupon(coupon)
                .member(member)
                .build();
        issueCouponRepository.save(issueCoupon);
    }
}
