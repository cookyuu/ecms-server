package com.cookyuu.ecms_server.domain.coupon.facade;

import com.cookyuu.ecms_server.domain.coupon.dto.IssueCouponDto;
import com.cookyuu.ecms_server.domain.coupon.entity.Coupon;
import com.cookyuu.ecms_server.domain.coupon.service.CouponService;
import com.cookyuu.ecms_server.domain.coupon.service.IssueCouponService;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.domain.ECMSCouponException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponFacade {
    private final CouponService couponService;
    private final MemberService memberService;
    private final IssueCouponService issueCouponService;

    public void issueCoupon(long memberId, IssueCouponDto.Request couponInfo) {
        Coupon coupon = couponService.issueCoupon(couponInfo);
        try {
            Member member = memberService.findMemberById(memberId);
            issueCouponService.issueCoupon(member, coupon);
        } catch (Exception e) {
            couponService.issueCouponFail(couponInfo);
            throw new ECMSCouponException(ResultCode.BAD_REQUEST, "쿠폰 발급 실패");
        }
    }
}
