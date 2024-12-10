package com.cookyuu.ecms_server.domain.coupon.facade;

import com.cookyuu.ecms_server.domain.coupon.entity.Coupon;
import com.cookyuu.ecms_server.domain.coupon.service.CouponService;
import com.cookyuu.ecms_server.domain.coupon.service.IssueCouponService;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.global.aop.redisson.DistributedLock;
import com.cookyuu.ecms_server.global.code.RedisKeyCode;
import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.domain.ECMSCouponException;
import com.cookyuu.ecms_server.global.utils.RedisUtils;
import com.cookyuu.ecms_server.global.utils.RedissonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponFacade {
    private final CouponService couponService;
    private final MemberService memberService;
    private final IssueCouponService issueCouponService;
    private final RedisTemplate redisTemplate;
    private final RedisUtils redisUtils;
    private final RedissonUtils redissonUtils;

    @DistributedLock(key = "'lock:coupon:' + #couponNumber + ':' + #memberId")
    public void issueCoupon(long memberId, String couponNumber) {
        try {
            validateRequest(couponNumber);
            isIssuable(memberId, couponNumber);
            processIssue(memberId, couponNumber);
            recordSuccess(couponNumber);
        } catch (Exception e) {
            log.info("[Coupon::Issue] Fail to issue coupon, ", e);
            recordFailure(couponNumber);
            throw new ECMSCouponException(ResultCode.BAD_REQUEST, "쿠폰 발급 실패");
        }
    }

    private void validateRequest(String couponNumber) {
        couponService.validateCoupon(couponNumber);
        log.debug("[Coupon::Issue] Validate Request process, OK!");
    }

    private void isIssuable(long memberId, String couponNumber) {
        String strMemberId = String.valueOf(memberId);
        String couponCount = (String) redisTemplate.opsForValue().get(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber);

        if (Boolean.TRUE.equals(redissonUtils.isIssuedCoupon(strMemberId, couponNumber))) {
            throw new ECMSCouponException(ResultCode.COUPON_ISSUE_FAIL, "이미 발급된 쿠폰입니다. ");
        }

        if (couponCount == null && Long.parseLong(couponCount) <= 0) {
            throw new ECMSCouponException(ResultCode.COUPON_SOLD_OUT);
        }
        log.debug("[Coupon::Issue] Check Is Issuable for redis, OK!");
    }

    protected void processIssue(Long memberId, String couponNumber) {
        String strMemberId = String.valueOf(memberId);
        redissonUtils.issueCoupon(strMemberId, couponNumber);
        processActualCouponIssue(memberId, couponNumber);
        log.debug("[Coupon::Issue] Issue process, OK!");

    }

    @Async
    @Transactional
    protected void processActualCouponIssue(Long memberId, String couponNumber) {
        Coupon coupon = couponService.findCouponByCouponNumber(couponNumber);
        Member member = memberService.findMemberById(memberId);
        int count;
        try {
            issueCouponService.issueCoupon(member, coupon);
            count = Integer.parseInt(redisUtils.getData(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber));
            coupon.issue(count);
            log.debug("[Coupon::Issue] Actual issue coupon process, OK!");
        } catch (Exception e) {
            log.error("[CouponIssue::Fail] Fail to Issue coupon, ", e);
            rollback(memberId, couponNumber);
            count = Integer.parseInt(redisUtils.getData(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber));
            coupon.issueFail(count);
            throw e;
        } finally {
            couponService.save(coupon);
        }
    }

    private void rollback(Long memberId, String couponNumber) {
        String strMemberId = String.valueOf(memberId);
        redissonUtils.issueCouponRollback(strMemberId, couponNumber);
        log.debug("[Coupon::Issue] Rollback Issue coupon.");
    }

    private void recordSuccess (String couponNumber) {
        redissonUtils.recordIssueCouponStatus(couponNumber, "success");
        log.debug("[Coupon::Issue] Issue coupon success record, OK!");
    }

    private void recordFailure(String couponNumber) {
        redissonUtils.recordIssueCouponStatus(couponNumber, "failure");
        log.debug("[Coupon::Issue] Issue coupon failure record, OK!");
    }


}
