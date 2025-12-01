package com.cookyuu.ecms_server.common.logging;

import org.slf4j.Logger;

import java.math.BigDecimal;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

/**
 * 구조화된 로깅을 위한 유틸리티 클래스
 * LogEvents와 LogFields를 활용한 JSON 로그 생성
 */
public final class StructuredLogger {

    private StructuredLogger() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    // ========== Order Logging Methods ==========

    public static void logOrderCreated(Logger log, Long orderId, String orderNumber, BigDecimal totalAmount, int itemCount) {
        log.info(LogEvents.ORDER_CREATED,
                keyValue(LogFields.ORDER_ID, orderId),
                keyValue(LogFields.ORDER_NUMBER, orderNumber),
                keyValue(LogFields.TOTAL_AMOUNT, totalAmount),
                keyValue(LogFields.ITEM_COUNT, itemCount)
        );
    }

    public static void logOrderCancelled(Logger log, Long orderId, String orderNumber, String cancelReason) {
        log.info(LogEvents.ORDER_CANCELLED,
                keyValue(LogFields.ORDER_ID, orderId),
                keyValue(LogFields.ORDER_NUMBER, orderNumber),
                keyValue(LogFields.CANCEL_REASON, cancelReason)
        );
    }

    public static void logOrderRevised(Logger log, Long orderId, String orderNumber, String orderStatus) {
        log.info(LogEvents.ORDER_REVISED,
                keyValue(LogFields.ORDER_ID, orderId),
                keyValue(LogFields.ORDER_NUMBER, orderNumber),
                keyValue(LogFields.ORDER_STATUS, orderStatus)
        );
    }

    public static void logOrderReviseError(Logger log, Long orderId, String orderStatus, String errorMessage) {
        log.warn(LogEvents.ORDER_REVISED,
                keyValue(LogFields.ORDER_ID, orderId),
                keyValue(LogFields.ORDER_STATUS, orderStatus),
                keyValue(LogFields.ERROR_MESSAGE, errorMessage)
        );
    }

    public static void logOrderConfirmed(Logger log, Long orderId, String orderNumber) {
        log.info(LogEvents.ORDER_CONFIRMED,
                keyValue(LogFields.ORDER_ID, orderId),
                keyValue(LogFields.ORDER_NUMBER, orderNumber)
        );
    }

    // ========== Payment Logging Methods ==========

    public static void logPaymentRequested(Logger log, String paymentNumber, String orderNumber, BigDecimal amount, String paymentMethod) {
        log.info(LogEvents.PAYMENT_REQUESTED,
                keyValue(LogFields.PAYMENT_NUMBER, paymentNumber),
                keyValue(LogFields.ORDER_NUMBER, orderNumber),
                keyValue(LogFields.PAYMENT_AMOUNT, amount),
                keyValue(LogFields.PAYMENT_METHOD, paymentMethod)
        );
    }

    public static void logPaymentCompleted(Logger log, Long paymentId, String paymentNumber, BigDecimal amount) {
        log.info(LogEvents.PAYMENT_COMPLETED,
                keyValue(LogFields.PAYMENT_ID, paymentId),
                keyValue(LogFields.PAYMENT_NUMBER, paymentNumber),
                keyValue(LogFields.PAYMENT_AMOUNT, amount)
        );
    }

    public static void logPaymentFailed(Logger log, String paymentNumber, String orderNumber, String errorMessage) {
        log.error(LogEvents.PAYMENT_FAILED,
                keyValue(LogFields.PAYMENT_NUMBER, paymentNumber),
                keyValue(LogFields.ORDER_NUMBER, orderNumber),
                keyValue(LogFields.ERROR_MESSAGE, errorMessage)
        );
    }

    public static void logPaymentCancelled(Logger log, String paymentNumber, String orderNumber, String cancelReason) {
        log.info(LogEvents.PAYMENT_CANCELLED,
                keyValue(LogFields.PAYMENT_NUMBER, paymentNumber),
                keyValue(LogFields.ORDER_NUMBER, orderNumber),
                keyValue(LogFields.CANCEL_REASON, cancelReason)
        );
    }

    // ========== Coupon Logging Methods ==========

    public static void logCouponCreated(Logger log, Long couponId, String couponCode, String couponType) {
        log.info(LogEvents.COUPON_CREATED,
                keyValue(LogFields.COUPON_ID, couponId),
                keyValue(LogFields.COUPON_CODE, couponCode),
                keyValue(LogFields.COUPON_TYPE, couponType)
        );
    }

    public static void logCouponIssued(Logger log, Long couponId, String couponNumber, Long userId) {
        log.info(LogEvents.COUPON_ISSUED,
                keyValue(LogFields.COUPON_ID, couponId),
                keyValue(LogFields.COUPON_NUMBER, couponNumber),
                keyValue(LogFields.USER_ID, userId)
        );
    }

    public static void logCouponIssueFailed(Logger log, Long couponId, Long userId, String errorMessage) {
        log.error(LogEvents.COUPON_ISSUE_FAILED,
                keyValue(LogFields.COUPON_ID, couponId),
                keyValue(LogFields.USER_ID, userId),
                keyValue(LogFields.ERROR_MESSAGE, errorMessage)
        );
    }

    public static void logCouponUsed(Logger log, String couponNumber, Long userId, BigDecimal discountPrice) {
        log.info(LogEvents.COUPON_USED,
                keyValue(LogFields.COUPON_NUMBER, couponNumber),
                keyValue(LogFields.USER_ID, userId),
                keyValue(LogFields.DISCOUNT_PRICE, discountPrice)
        );
    }

    // ========== Member Logging Methods ==========

    public static void logMemberRegistered(Logger log, Long memberId, String loginId, String email) {
        log.info(LogEvents.MEMBER_REGISTERED,
                keyValue(LogFields.MEMBER_ID, memberId),
                keyValue(LogFields.LOGIN_ID, loginId),
                keyValue(LogFields.EMAIL, email)
        );
    }

    public static void logMemberUpdated(Logger log, Long memberId, String loginId) {
        log.info(LogEvents.MEMBER_UPDATED,
                keyValue(LogFields.MEMBER_ID, memberId),
                keyValue(LogFields.LOGIN_ID, loginId)
        );
    }

    public static void logMemberDeleted(Logger log, Long memberId, String loginId) {
        log.info(LogEvents.MEMBER_DELETED,
                keyValue(LogFields.MEMBER_ID, memberId),
                keyValue(LogFields.LOGIN_ID, loginId)
        );
    }

    public static void logMemberRoleChanged(Logger log, Long memberId, String oldRole, String newRole) {
        log.info(LogEvents.MEMBER_ROLE_CHANGED,
                keyValue(LogFields.MEMBER_ID, memberId),
                keyValue("old_role", oldRole),
                keyValue("new_role", newRole)
        );
    }

    // ========== Auth Logging Methods ==========

    public static void logLoginSuccess(Logger log, Long userId, String loginId, String ipAddress) {
        log.info(LogEvents.LOGIN_SUCCESS,
                keyValue(LogFields.USER_ID, userId),
                keyValue(LogFields.LOGIN_ID, loginId),
                keyValue(LogFields.IP_ADDRESS, ipAddress)
        );
    }

    public static void logLoginFailed(Logger log, String loginId, String ipAddress, String reason) {
        log.warn(LogEvents.LOGIN_FAILED,
                keyValue(LogFields.LOGIN_ID, loginId),
                keyValue(LogFields.IP_ADDRESS, ipAddress),
                keyValue(LogFields.ERROR_MESSAGE, reason)
        );
    }

    public static void logTokenIssued(Logger log, Long userId, String tokenType) {
        log.debug(LogEvents.TOKEN_ISSUED,
                keyValue(LogFields.USER_ID, userId),
                keyValue(LogFields.TOKEN_TYPE, tokenType)
        );
    }

    // ========== Product Logging Methods ==========

    public static void logProductRegistered(Logger log, Long productId, String productName, BigDecimal price) {
        log.info(LogEvents.PRODUCT_REGISTERED,
                keyValue(LogFields.PRODUCT_ID, productId),
                keyValue(LogFields.PRODUCT_NAME, productName),
                keyValue(LogFields.PRODUCT_PRICE, price)
        );
    }

    public static void logProductOutOfStock(Logger log, Long productId, String productName) {
        log.warn(LogEvents.PRODUCT_OUT_OF_STOCK,
                keyValue(LogFields.PRODUCT_ID, productId),
                keyValue(LogFields.PRODUCT_NAME, productName)
        );
    }

    // ========== Error Logging Methods ==========

    public static void logBusinessError(Logger log, String errorCode, String errorMessage) {
        log.error(LogEvents.BUSINESS_ERROR,
                keyValue(LogFields.ERROR_CODE, errorCode),
                keyValue(LogFields.ERROR_MESSAGE, errorMessage)
        );
    }

    public static void logBusinessError(Logger log, String errorCode, String errorMessage, Exception e) {
        log.error(LogEvents.BUSINESS_ERROR,
                keyValue(LogFields.ERROR_CODE, errorCode),
                keyValue(LogFields.ERROR_MESSAGE, errorMessage),
                e
        );
    }

    public static void logValidationError(Logger log, String field, String errorMessage) {
        log.warn(LogEvents.VALIDATION_ERROR,
                keyValue("field", field),
                keyValue(LogFields.ERROR_MESSAGE, errorMessage)
        );
    }

    // ========== Performance Logging Methods ==========

    public static void logSlowQuery(Logger log, String operation, long queryTimeMs) {
        log.warn(LogEvents.SLOW_QUERY,
                keyValue(LogFields.OPERATION, operation),
                keyValue(LogFields.QUERY_TIME_MS, queryTimeMs)
        );
    }

    public static void logDistributedLockAcquired(Logger log, String lockKey, long lockWaitMs) {
        log.debug(LogEvents.DISTRIBUTED_LOCK_ACQUIRED,
                keyValue(LogFields.LOCK_KEY, lockKey),
                keyValue(LogFields.LOCK_WAIT_MS, lockWaitMs)
        );
    }

    public static void logDistributedLockFailed(Logger log, String lockKey, String errorMessage) {
        log.error(LogEvents.DISTRIBUTED_LOCK_FAILED,
                keyValue(LogFields.LOCK_KEY, lockKey),
                keyValue(LogFields.ERROR_MESSAGE, errorMessage)
        );
    }

    // ========== Generic Logging with Custom Fields ==========

    public static void logEvent(Logger log, String event, Object... keyValues) {
        log.info(event, keyValues);
    }

    public static void logDebug(Logger log, String event, Object... keyValues) {
        log.debug(event, keyValues);
    }

    public static void logWarn(Logger log, String event, Object... keyValues) {
        log.warn(event, keyValues);
    }

    public static void logError(Logger log, String event, Object... keyValues) {
        log.error(event, keyValues);
    }
}
