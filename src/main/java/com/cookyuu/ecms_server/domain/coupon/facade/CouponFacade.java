package com.cookyuu.ecms_server.domain.coupon.facade;

import com.cookyuu.ecms_server.domain.coupon.entity.Coupon;
import com.cookyuu.ecms_server.domain.coupon.logging.CouponLogHelper;
import com.cookyuu.ecms_server.domain.coupon.service.CouponService;
import com.cookyuu.ecms_server.domain.coupon.service.IssueCouponService;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.common.aop.DistributedLock;
import com.cookyuu.ecms_server.common.enums.RedisKeyCode;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.utils.RedisUtils;
import com.cookyuu.ecms_server.common.utils.RedissonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponFacade {
    private final CouponService couponService;
    private final MemberService memberService;
    private final IssueCouponService issueCouponService;
    private final RedisTemplate redisTemplate;
    private final RedisUtils redisUtils;
    private final RedissonUtils redissonUtils;
    private final CouponLogHelper couponLogHelper;

    @DistributedLock(key = "'lock:coupon:' + #couponNumber + ':' + #memberId")
    public void issueCoupon(long memberId, String couponNumber) {
        long startTime = System.currentTimeMillis();

        try {
            validateRequest(couponNumber);
            isIssuable(memberId, couponNumber);
            processIssue(memberId, couponNumber);
            recordSuccess(couponNumber);

            couponLogHelper.logCouponIssued(memberId, couponNumber, System.currentTimeMillis() - startTime);

        } catch (BusinessException e) {
            recordFailure(couponNumber);

            couponLogHelper.logCouponIssueFailed(memberId, couponNumber, e.getResultCode(),
                e.getMessage(), System.currentTimeMillis() - startTime, e);

            throw new BusinessException(ResultCode.BAD_REQUEST, "쿠폰 발급 실패");
        }
    }

    private void validateRequest(String couponNumber) {
        couponService.validateCoupon(couponNumber);
        couponLogHelper.logCouponValidationPassed(couponNumber);
    }

    private void isIssuable(long memberId, String couponNumber) {
        String strMemberId = String.valueOf(memberId);
        String couponCount = (String) redisTemplate.opsForValue().get(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber);

        if (Boolean.TRUE.equals(redissonUtils.isIssuedCoupon(strMemberId, couponNumber))) {
            couponLogHelper.logCouponDuplicateIssue(memberId, couponNumber, ResultCode.COUPON_ISSUE_FAIL);
            throw new BusinessException(ResultCode.COUPON_ISSUE_FAIL, "이미 발급된 쿠폰입니다. ");
        }

        if (couponCount == null || Long.parseLong(couponCount) <= 0) {
            couponLogHelper.logCouponSoldOut(memberId, couponNumber, couponCount, ResultCode.COUPON_SOLD_OUT);
            throw new BusinessException(ResultCode.COUPON_SOLD_OUT);
        }
        couponLogHelper.logCouponIssuableValidationPassed(memberId, couponNumber, couponCount);
    }

    protected void processIssue(Long memberId, String couponNumber) {
        String strMemberId = String.valueOf(memberId);
        redissonUtils.issueCoupon(strMemberId, couponNumber);
        processActualCouponIssue(memberId, couponNumber);
        couponLogHelper.logCouponIssueProcessed(memberId, couponNumber);
    }

    @Transactional
    protected void processActualCouponIssue(Long memberId, String couponNumber) {
        Coupon coupon = couponService.findCouponByCouponNumber(couponNumber);
        Member member = memberService.findMemberById(memberId);
        int count;
        try {
            issueCouponService.issueCoupon(member, coupon);
            count = Integer.parseInt(redisUtils.getData(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber));
            coupon.issue(count);
            couponLogHelper.logActualCouponIssueProcessed(memberId, coupon.getId(), couponNumber, count);
        } catch (Exception e) {
            couponLogHelper.logActualCouponIssueFailed(memberId, coupon.getId(), couponNumber, e.getMessage(), e);
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
        couponLogHelper.logCouponIssueRollback(memberId, couponNumber);
    }

    private void recordSuccess (String couponNumber) {
        redissonUtils.recordIssueCouponStatus(couponNumber, "success");
        couponLogHelper.logCouponIssueSuccessRecorded(couponNumber);
    }

    private void recordFailure(String couponNumber) {
        redissonUtils.recordIssueCouponStatus(couponNumber, "failure");
        couponLogHelper.logCouponIssueFailureRecorded(couponNumber);
    }


}
