package com.cookyuu.ecms_server.domain.coupon.facade;

import com.cookyuu.ecms_server.domain.coupon.entity.Coupon;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

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
        long startTime = System.currentTimeMillis();

        try {
            validateRequest(couponNumber);
            isIssuable(memberId, couponNumber);
            processIssue(memberId, couponNumber);
            recordSuccess(couponNumber);

            log.atInfo()
                .addKeyValue(EVENT, COUPON_ISSUED)
                .addKeyValue(USER_ID, memberId)
                .addKeyValue(COUPON_NUMBER, couponNumber)
                .addKeyValue(STATUS, "success")
                .addKeyValue(DURATION_MS, System.currentTimeMillis() - startTime)
                .log("Coupon issued successfully");

        } catch (BusinessException e) {
            recordFailure(couponNumber);

            log.atError()
                .addKeyValue(EVENT, COUPON_ISSUE_FAILED)
                .addKeyValue(USER_ID, memberId)
                .addKeyValue(COUPON_NUMBER, couponNumber)
                .addKeyValue(ERROR_CODE, e.getResultCode().getCode())
                .addKeyValue(ERROR_MESSAGE, e.getMessage())
                .addKeyValue(STATUS, "failure")
                .addKeyValue(DURATION_MS, System.currentTimeMillis() - startTime)
                .setCause(e)
                .log("Coupon issue failed");

            throw new BusinessException(ResultCode.BAD_REQUEST, "쿠폰 발급 실패");
        }
    }

    private void validateRequest(String couponNumber) {
        couponService.validateCoupon(couponNumber);
        log.atDebug()
            .addKeyValue("operation", "validateCouponRequest")
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .log("Coupon validation passed");
    }

    private void isIssuable(long memberId, String couponNumber) {
        String strMemberId = String.valueOf(memberId);
        String couponCount = (String) redisTemplate.opsForValue().get(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber);

        if (Boolean.TRUE.equals(redissonUtils.isIssuedCoupon(strMemberId, couponNumber))) {
            log.atWarn()
                .addKeyValue(EVENT, COUPON_ISSUE_FAILED)
                .addKeyValue(USER_ID, memberId)
                .addKeyValue(COUPON_NUMBER, couponNumber)
                .addKeyValue(ERROR_CODE, ResultCode.COUPON_ISSUE_FAIL.getCode())
                .addKeyValue(ERROR_MESSAGE, "Coupon already issued to this user")
                .log("Coupon issue validation failed - duplicate issue");
            throw new BusinessException(ResultCode.COUPON_ISSUE_FAIL, "이미 발급된 쿠폰입니다. ");
        }

        if (couponCount == null || Long.parseLong(couponCount) <= 0) {
            log.atWarn()
                .addKeyValue(EVENT, COUPON_ISSUE_FAILED)
                .addKeyValue(USER_ID, memberId)
                .addKeyValue(COUPON_NUMBER, couponNumber)
                .addKeyValue("available_count", couponCount)
                .addKeyValue(ERROR_CODE, ResultCode.COUPON_SOLD_OUT.getCode())
                .log("Coupon issue validation failed - sold out");
            throw new BusinessException(ResultCode.COUPON_SOLD_OUT);
        }
        log.atDebug()
            .addKeyValue("operation", "checkIssuable")
            .addKeyValue(USER_ID, memberId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue("available_count", couponCount)
            .log("Coupon issuable validation passed");
    }

    protected void processIssue(Long memberId, String couponNumber) {
        String strMemberId = String.valueOf(memberId);
        redissonUtils.issueCoupon(strMemberId, couponNumber);
        processActualCouponIssue(memberId, couponNumber);
        log.atDebug()
            .addKeyValue("operation", "processIssue")
            .addKeyValue(USER_ID, memberId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .log("Coupon issue process completed");

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
            log.atDebug()
                .addKeyValue("operation", "processActualCouponIssue")
                .addKeyValue(USER_ID, memberId)
                .addKeyValue(COUPON_ID, coupon.getId())
                .addKeyValue(COUPON_NUMBER, couponNumber)
                .addKeyValue("remaining_count", count)
                .log("Actual coupon issue process completed");
        } catch (Exception e) {
            log.atError()
                .addKeyValue(EVENT, SYSTEM_ERROR)
                .addKeyValue(USER_ID, memberId)
                .addKeyValue(COUPON_ID, coupon.getId())
                .addKeyValue(COUPON_NUMBER, couponNumber)
                .addKeyValue(ERROR_MESSAGE, e.getMessage())
                .setCause(e)
                .log("Actual coupon issue failed - transaction error");
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
        log.atDebug()
            .addKeyValue("operation", "rollbackCouponIssue")
            .addKeyValue(USER_ID, memberId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .log("Coupon issue rollback completed");
    }

    private void recordSuccess (String couponNumber) {
        redissonUtils.recordIssueCouponStatus(couponNumber, "success");
        log.atDebug()
            .addKeyValue("operation", "recordIssueCouponStatus")
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue(STATUS, "success")
            .log("Coupon issue success recorded");
    }

    private void recordFailure(String couponNumber) {
        redissonUtils.recordIssueCouponStatus(couponNumber, "failure");
        log.atDebug()
            .addKeyValue("operation", "recordIssueCouponStatus")
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue(STATUS, "failure")
            .log("Coupon issue failure recorded");
    }


}
