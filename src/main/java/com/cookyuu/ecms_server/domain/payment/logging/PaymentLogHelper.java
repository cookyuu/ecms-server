package com.cookyuu.ecms_server.domain.payment.logging;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Slf4j
@Component
public class PaymentLogHelper {

    /**
     * 결제 완료 Info 로그
     */
    public void logPaymentCompleted(Long userId, Long orderId, String orderNumber, String paymentNumber,
                                    String paymentMethod, int paymentAmount, long durationMs) {
        log.atInfo()
            .addKeyValue(EVENT, PAYMENT_COMPLETED)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(ORDER_ID, orderId)
            .addKeyValue(ORDER_NUMBER, orderNumber)
            .addKeyValue(PAYMENT_NUMBER, paymentNumber)
            .addKeyValue(PAYMENT_METHOD, paymentMethod)
            .addKeyValue(PAYMENT_AMOUNT, paymentAmount)
            .addKeyValue(DURATION_MS, durationMs)
            .log("Payment completed successfully");
    }

    /**
     * 결제 실패 - 금액 불일치 Warn 로그
     */
    public void logPaymentFailedAmountMismatch(Long userId, Long orderId, String orderNumber, String paymentNumber,
                                               String paymentMethod, int requestedAmount, int expectedAmount,
                                               ResultCode resultCode, long durationMs) {
        log.atWarn()
            .addKeyValue(EVENT, PAYMENT_FAILED)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(ORDER_ID, orderId)
            .addKeyValue(ORDER_NUMBER, orderNumber)
            .addKeyValue(PAYMENT_NUMBER, paymentNumber)
            .addKeyValue(PAYMENT_METHOD, paymentMethod)
            .addKeyValue(REQUESTED_AMOUNT, requestedAmount)
            .addKeyValue(EXPECTED_AMOUNT, expectedAmount)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .addKeyValue(ERROR_MESSAGE, "Payment amount mismatch")
            .addKeyValue(DURATION_MS, durationMs)
            .log("Payment failed - amount mismatch");
    }

    /**
     * 결제 취소 요청 Debug 로그
     */
    public void logPaymentCancellationRequested(Long userId, String orderNumber, String paymentNumber, String cancelReason) {
        log.atDebug()
            .addKeyValue(EVENT, PAYMENT_CANCELLED)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(ORDER_NUMBER, orderNumber)
            .addKeyValue(PAYMENT_NUMBER, paymentNumber)
            .addKeyValue(CANCEL_REASON, cancelReason)
            .log("Payment cancellation requested");
    }

    /**
     * 결제 취소 성공 Info 로그
     */
    public void logPaymentCancelled(Long userId, String orderNumber, String paymentNumber,
                                    String cancelReason, long durationMs) {
        log.atInfo()
            .addKeyValue(EVENT, PAYMENT_CANCELLED)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(ORDER_NUMBER, orderNumber)
            .addKeyValue(PAYMENT_NUMBER, paymentNumber)
            .addKeyValue(CANCEL_REASON, cancelReason)
            .addKeyValue(DURATION_MS, durationMs)
            .log("Payment cancelled successfully");
    }

    /**
     * 결제 상세 조회 Debug 로그
     */
    public void logPaymentDetailFetch(Long userId, String userRole, String paymentNumber) {
        log.atDebug()
            .addKeyValue("operation", "getPaymentDetail")
            .addKeyValue(USER_ID, userId)
            .addKeyValue(USER_ROLE, userRole)
            .addKeyValue(PAYMENT_NUMBER, paymentNumber)
            .log("Fetching payment detail");
    }

    /**
     * 결제 상세 조회 완료 Debug 로그
     */
    public void logPaymentDetailFetched(Long userId, String paymentNumber) {
        log.atDebug()
            .addKeyValue("operation", "getPaymentDetail")
            .addKeyValue(USER_ID, userId)
            .addKeyValue(PAYMENT_NUMBER, paymentNumber)
            .log("Payment detail fetched successfully");
    }

    /**
     * 결제 가능 여부 검증 실패 Warn 로그
     */
    public void logPaymentValidationFailed(Long orderId, String orderStatus, ResultCode resultCode) {
        log.atWarn()
            .addKeyValue(EVENT, VALIDATION_ERROR)
            .addKeyValue(ORDER_ID, orderId)
            .addKeyValue(ORDER_STATUS, orderStatus)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .log("Payment validation failed - invalid order status");
    }

    /**
     * 결제 가능 여부 검증 성공 Debug 로그
     */
    public void logPaymentValidationPassed(Long orderId, String orderStatus) {
        log.atDebug()
            .addKeyValue("operation", "checkPossiblePayment")
            .addKeyValue(ORDER_ID, orderId)
            .addKeyValue(ORDER_STATUS, orderStatus)
            .log("Payment validation passed");
    }

    /**
     * 결제 취소 가능 여부 검증 실패 Warn 로그
     */
    public void logPaymentCancellationValidationFailed(Long orderId, String orderStatus, ResultCode resultCode) {
        log.atWarn()
            .addKeyValue(EVENT, VALIDATION_ERROR)
            .addKeyValue(ORDER_ID, orderId)
            .addKeyValue(ORDER_STATUS, orderStatus)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .log("Payment cancellation validation failed - invalid order status");
    }

    /**
     * 결제 취소 가능 여부 검증 성공 Debug 로그
     */
    public void logPaymentCancellationValidationPassed(Long orderId, String orderStatus) {
        log.atDebug()
            .addKeyValue("operation", "checkPossiblePaymentCancel")
            .addKeyValue(ORDER_ID, orderId)
            .addKeyValue(ORDER_STATUS, orderStatus)
            .log("Payment cancellation validation passed");
    }
}
