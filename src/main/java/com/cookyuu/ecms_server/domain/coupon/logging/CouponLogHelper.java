package com.cookyuu.ecms_server.domain.coupon.logging;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Slf4j
@Component
public class CouponLogHelper {

    /**
     * 쿠폰 발급 성공 Info 로그
     */
    public void logCouponIssued(Long userId, String couponNumber, long durationMs) {
        log.atInfo()
            .addKeyValue(EVENT, COUPON_ISSUED)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue(STATUS, "success")
            .addKeyValue(DURATION_MS, durationMs)
            .log("Coupon issued successfully");
    }

    /**
     * 쿠폰 발급 실패 Error 로그
     */
    public void logCouponIssueFailed(Long userId, String couponNumber, ResultCode resultCode,
                                     String errorMessage, long durationMs, Exception e) {
        log.atError()
            .addKeyValue(EVENT, COUPON_ISSUE_FAILED)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .addKeyValue(ERROR_MESSAGE, errorMessage)
            .addKeyValue(STATUS, "failure")
            .addKeyValue(DURATION_MS, durationMs)
            .setCause(e)
            .log("Coupon issue failed");
    }

    /**
     * 쿠폰 검증 성공 Debug 로그
     */
    public void logCouponValidationPassed(String couponNumber) {
        log.atDebug()
            .addKeyValue("operation", "validateCouponRequest")
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .log("Coupon validation passed");
    }

    /**
     * 쿠폰 발급 가능 여부 검증 - 중복 발급 Warn 로그
     */
    public void logCouponDuplicateIssue(Long userId, String couponNumber, ResultCode resultCode) {
        log.atWarn()
            .addKeyValue(EVENT, COUPON_ISSUE_FAILED)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .addKeyValue(ERROR_MESSAGE, "Coupon already issued to this user")
            .log("Coupon issue validation failed - duplicate issue");
    }

    /**
     * 쿠폰 발급 가능 여부 검증 - 쿠폰 소진 Warn 로그
     */
    public void logCouponSoldOut(Long userId, String couponNumber, String availableCount, ResultCode resultCode) {
        log.atWarn()
            .addKeyValue(EVENT, COUPON_ISSUE_FAILED)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue("available_count", availableCount)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .log("Coupon issue validation failed - sold out");
    }

    /**
     * 쿠폰 발급 가능 여부 검증 성공 Debug 로그
     */
    public void logCouponIssuableValidationPassed(Long userId, String couponNumber, String availableCount) {
        log.atDebug()
            .addKeyValue("operation", "checkIssuable")
            .addKeyValue(USER_ID, userId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue("available_count", availableCount)
            .log("Coupon issuable validation passed");
    }

    /**
     * 쿠폰 발급 처리 완료 Debug 로그
     */
    public void logCouponIssueProcessed(Long userId, String couponNumber) {
        log.atDebug()
            .addKeyValue("operation", "processIssue")
            .addKeyValue(USER_ID, userId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .log("Coupon issue process completed");
    }

    /**
     * 실제 쿠폰 발급 처리 완료 Debug 로그
     */
    public void logActualCouponIssueProcessed(Long userId, Long couponId, String couponNumber, int remainingCount) {
        log.atDebug()
            .addKeyValue("operation", "processActualCouponIssue")
            .addKeyValue(USER_ID, userId)
            .addKeyValue(COUPON_ID, couponId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue("remaining_count", remainingCount)
            .log("Actual coupon issue process completed");
    }

    /**
     * 실제 쿠폰 발급 실패 Error 로그
     */
    public void logActualCouponIssueFailed(Long userId, Long couponId, String couponNumber,
                                           String errorMessage, Exception e) {
        log.atError()
            .addKeyValue(EVENT, SYSTEM_ERROR)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(COUPON_ID, couponId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue(ERROR_MESSAGE, errorMessage)
            .setCause(e)
            .log("Actual coupon issue failed - transaction error");
    }

    /**
     * 쿠폰 발급 롤백 Debug 로그
     */
    public void logCouponIssueRollback(Long userId, String couponNumber) {
        log.atDebug()
            .addKeyValue("operation", "rollbackCouponIssue")
            .addKeyValue(USER_ID, userId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .log("Coupon issue rollback completed");
    }

    /**
     * 쿠폰 발급 성공 상태 기록 Debug 로그
     */
    public void logCouponIssueSuccessRecorded(String couponNumber) {
        log.atDebug()
            .addKeyValue("operation", "recordIssueCouponStatus")
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue(STATUS, "success")
            .log("Coupon issue success recorded");
    }

    /**
     * 쿠폰 발급 실패 상태 기록 Debug 로그
     */
    public void logCouponIssueFailureRecorded(String couponNumber) {
        log.atDebug()
            .addKeyValue("operation", "recordIssueCouponStatus")
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue(STATUS, "failure")
            .log("Coupon issue failure recorded");
    }
}
