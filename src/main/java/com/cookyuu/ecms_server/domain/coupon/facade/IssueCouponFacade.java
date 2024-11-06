package com.cookyuu.ecms_server.domain.coupon.facade;

import com.cookyuu.ecms_server.domain.coupon.dto.IssueCouponDto;
import com.cookyuu.ecms_server.domain.coupon.entity.Coupon;
import com.cookyuu.ecms_server.domain.coupon.service.CouponService;
import com.cookyuu.ecms_server.domain.coupon.service.IssueCouponService;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IssueCouponFacade {
    private final CouponService couponService;
    private final MemberService memberService;
    private final IssueCouponService issueCouponService;

    @Transactional
    public IssueCouponDto.Response issueCoupon(Long userId, IssueCouponDto.Request couponInfo) {
        Coupon coupon = couponService.issueCoupon(couponInfo);
        Member member = memberService.findMemberById(userId);
        issueCouponService.issueCoupon(member, coupon);
        return null;
    }
}
