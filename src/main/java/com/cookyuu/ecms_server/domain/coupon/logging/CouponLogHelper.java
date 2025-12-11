package com.cookyuu.ecms_server.domain.coupon.logging;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Slf4j
@Component
public class CouponLogHelper {

    public void logCouponIssued(Long userId, String couponNumber, long durationMs) {
        log.atInfo()
            .addKeyValue(EVENT, COUPON_ISSUED)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue(STATUS, "success")
            .addKeyValue(DURATION_MS, durationMs)
            .log("Coupon issued successfully");
    }

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

    public void logCouponValidationPassed(String couponNumber) {
        log.atDebug()
            .addKeyValue("operation", "validateCouponRequest")
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .log("Coupon validation passed");
    }

    public void logCouponDuplicateIssue(Long userId, String couponNumber, ResultCode resultCode) {
        log.atWarn()
            .addKeyValue(EVENT, COUPON_ISSUE_FAILED)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .addKeyValue(ERROR_MESSAGE, "Coupon already issued to this user")
            .log("Coupon issue validation failed - duplicate issue");
    }

    public void logCouponSoldOut(Long userId, String couponNumber, String availableCount, ResultCode resultCode) {
        log.atWarn()
            .addKeyValue(EVENT, COUPON_ISSUE_FAILED)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue("available_count", availableCount)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .log("Coupon issue validation failed - sold out");
    }

    public void logCouponIssuableValidationPassed(Long userId, String couponNumber, String availableCount) {
        log.atDebug()
            .addKeyValue("operation", "checkIssuable")
            .addKeyValue(USER_ID, userId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue("available_count", availableCount)
            .log("Coupon issuable validation passed");
    }

    public void logCouponIssueProcessed(Long userId, String couponNumber) {
        log.atDebug()
            .addKeyValue("operation", "processIssue")
            .addKeyValue(USER_ID, userId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .log("Coupon issue process completed");
    }

    public void logActualCouponIssueProcessed(Long userId, Long couponId, String couponNumber, int remainingCount) {
        log.atDebug()
            .addKeyValue("operation", "processActualCouponIssue")
            .addKeyValue(USER_ID, userId)
            .addKeyValue(COUPON_ID, couponId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue("remaining_count", remainingCount)
            .log("Actual coupon issue process completed");
    }

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

    public void logCouponIssueRollback(Long userId, String couponNumber) {
        log.atDebug()
            .addKeyValue("operation", "rollbackCouponIssue")
            .addKeyValue(USER_ID, userId)
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .log("Coupon issue rollback completed");
    }

    public void logCouponIssueSuccessRecorded(String couponNumber) {
        log.atDebug()
            .addKeyValue("operation", "recordIssueCouponStatus")
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue(STATUS, "success")
            .log("Coupon issue success recorded");
    }

    public void logCouponIssueFailureRecorded(String couponNumber) {
        log.atDebug()
            .addKeyValue("operation", "recordIssueCouponStatus")
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue(STATUS, "failure")
            .log("Coupon issue failure recorded");
    }
}
