package com.cookyuu.ecms_server.domain.coupon.facade;

import com.cookyuu.ecms_server.domain.coupon.dto.IssueCouponDto;
import com.cookyuu.ecms_server.domain.coupon.entity.Coupon;
import com.cookyuu.ecms_server.domain.coupon.repository.CouponRepository;
import com.cookyuu.ecms_server.domain.coupon.service.CouponService;
import com.cookyuu.ecms_server.domain.coupon.service.IssueCouponService;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponFacade {
    private final CouponService couponService;
    private final MemberService memberService;
    private final IssueCouponService issueCouponService;

    @Transactional
    public void issueCoupon(long memberId, IssueCouponDto.Request couponInfo) {

        Coupon coupon = couponService.issueCoupon(couponInfo);
        Member member = memberService.findMemberById(memberId);
        issueCouponService.issueCoupon(member, coupon);
    }
}
