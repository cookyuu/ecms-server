package com.cookyuu.ecms_server.domain.order.logging;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Slf4j
@Component
public class OrderLogHelper {

    /**
     * 재고 부족 에러 로그
     */
    public void logStockError(int quantity, int stockQuantity, ResultCode resultCode, String message) {
        log.atError()
            .addKeyValue(EVENT, PRODUCT_OUT_OF_STOCK)
            .addKeyValue(STOCK_QUANTITY, stockQuantity)
            .addKeyValue(QUANTITY, quantity)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .log(message);
    }

    /**
     * 가격 검증 에러 로그
     */
    public void logPriceError(Long productId, int orderPrice, Integer currentPrice, ResultCode resultCode, String message) {
        log.atError()
            .addKeyValue(EVENT, VALIDATION_ERROR)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue("order_price", orderPrice)
            .addKeyValue("current_price", currentPrice)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .log(message);
    }

    /**
     * 검증 성공 Debug 로그 (간결한 형태)
     */
    public void logValidationSuccess(String operation, Long productId, Integer quantity, Integer stockQuantity) {
        log.atDebug()
            .addKeyValue("operation", operation)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(QUANTITY, quantity)
            .addKeyValue(STOCK_QUANTITY, stockQuantity)
            .log("Validation passed");
    }

    /**
     * 재고 차감 Debug 로그
     */
    public void logStockDecrease(String operation, Long productId, int quantity, Integer stockQuantity) {
        log.atDebug()
            .addKeyValue("operation", operation)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(QUANTITY, quantity)
            .addKeyValue(STOCK_QUANTITY, stockQuantity)
            .log("Stock decreased");
    }

    /**
     * 재고 복구 Debug 로그
     */
    public void logStockRestore(String operation, Long productId, int quantity, Integer stockQuantity) {
        log.atDebug()
            .addKeyValue("operation", operation)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(QUANTITY, quantity)
            .addKeyValue(STOCK_QUANTITY, stockQuantity)
            .log("Stock restored");
    }

    /**
     * 주문 수정 시 재고 복구 Info 로그
     */
    public void logStockRestoreForRevision(Long productId, int quantity, Integer stockQuantity) {
        log.atDebug()
            .addKeyValue(EVENT, ORDER_REVISED)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(QUANTITY, quantity)
            .addKeyValue(STOCK_QUANTITY, stockQuantity)
            .log("Restored product quantity for order revision");
    }

    /**
     * 주문 수정 시 재고 차감 Debug 로그
     */
    public void logStockDecreaseForRevision(Long productId, int quantity, Integer stockQuantity) {
        log.atDebug()
            .addKeyValue(EVENT, ORDER_REVISED)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(QUANTITY, quantity)
            .addKeyValue(STOCK_QUANTITY, stockQuantity)
            .log("Stock decreased for order revision");
    }

    // ===== 주문 생성 관련 로그 =====

    /**
     * 상품을 찾을 수 없음 에러 로그
     */
    public void logProductNotFoundError(Long userId, Long productId) {
        log.atError()
            .addKeyValue(EVENT, BUSINESS_ERROR)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(ERROR_CODE, ResultCode.PRODUCT_NOT_FOUND.getCode())
            .log("Order creation failed - product not found");
    }

    /**
     * 주문 생성 성공 Info 로그
     */
    public void logOrderCreated(Long userId, Long orderId, String orderNumber, int totalAmount,
                                int itemCount, String orderStatus, long durationMs) {
        log.atInfo()
            .addKeyValue(EVENT, ORDER_CREATED)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(ORDER_ID, orderId)
            .addKeyValue(ORDER_NUMBER, orderNumber)
            .addKeyValue(TOTAL_AMOUNT, totalAmount)
            .addKeyValue(ITEM_COUNT, itemCount)
            .addKeyValue(ORDER_STATUS, orderStatus)
            .addKeyValue(DURATION_MS, durationMs)
            .log("Order created successfully");
    }

    /**
     * 주문 생성 실패 Error 로그
     */
    public void logOrderCreationFailed(Long userId, String orderNumber, String errorMessage,
                                       long durationMs, Exception e) {
        log.atError()
            .addKeyValue(EVENT, SYSTEM_ERROR)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(ORDER_NUMBER, orderNumber)
            .addKeyValue(ERROR_MESSAGE, errorMessage)
            .addKeyValue(DURATION_MS, durationMs)
            .setCause(e)
            .log("Order creation failed - transaction error");
    }

    // ===== 주문 취소 관련 로그 =====

    /**
     * 주문 취소 성공 Info 로그
     */
    public void logOrderCancelled(Long userId, Long orderId, String orderNumber,
                                  String orderStatus, String cancelReason, long durationMs) {
        log.atInfo()
            .addKeyValue(EVENT, ORDER_CANCELLED)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(ORDER_ID, orderId)
            .addKeyValue(ORDER_NUMBER, orderNumber)
            .addKeyValue(ORDER_STATUS, orderStatus)
            .addKeyValue(CANCEL_REASON, cancelReason)
            .addKeyValue(DURATION_MS, durationMs)
            .log("Order cancelled successfully");
    }

    /**
     * 주문 취소 실패 Warn 로그
     */
    public void logOrderCancellationFailed(Long userId, Long orderId, String orderNumber,
                                           String orderStatus, ResultCode resultCode) {
        log.atWarn()
            .addKeyValue(EVENT, BUSINESS_ERROR)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(ORDER_ID, orderId)
            .addKeyValue(ORDER_NUMBER, orderNumber)
            .addKeyValue(ORDER_STATUS, orderStatus)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .addKeyValue(ERROR_MESSAGE, "Cannot cancel order in current status")
            .log("Order cancellation failed - invalid status");
    }

    // ===== 주문 수정 관련 로그 =====

    /**
     * 주문 수정 실패 Warn 로그
     */
    public void logOrderRevisionFailed(Long orderId, String orderStatus, ResultCode resultCode) {
        log.atWarn()
            .addKeyValue(EVENT, BUSINESS_ERROR)
            .addKeyValue(ORDER_ID, orderId)
            .addKeyValue(ORDER_STATUS, orderStatus)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .addKeyValue(ERROR_MESSAGE, "Cannot revise order in current status")
            .log("Order revision failed - invalid status");
    }

    /**
     * 주문 수정 성공 Info 로그
     */
    public void logOrderRevised(Long userId, Long orderId, String orderNumber,
                                int totalAmount, int itemCount) {
        log.atInfo()
            .addKeyValue(EVENT, ORDER_REVISED)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(ORDER_ID, orderId)
            .addKeyValue(ORDER_NUMBER, orderNumber)
            .addKeyValue(TOTAL_AMOUNT, totalAmount)
            .addKeyValue(ITEM_COUNT, itemCount)
            .log("Order revised successfully");
    }

    // ===== Debug 로그 =====

    /**
     * 주문 상세 조회 Debug 로그
     */
    public void logOrderDetailFetch(String userRole, String orderNumber) {
        log.atDebug()
            .addKeyValue("operation", "getOrderDetail")
            .addKeyValue(USER_ROLE, userRole)
            .addKeyValue(ORDER_NUMBER, orderNumber)
            .log("Fetching order detail");
    }

    /**
     * 장바구니 업데이트 Debug 로그
     */
    public void logCartUpdate(Long cartId, Long productId) {
        log.atDebug()
            .addKeyValue("operation", "updateCart")
            .addKeyValue(CART_ID, cartId)
            .addKeyValue(PRODUCT_ID, productId)
            .log("Updating cart with ordered items");
    }

    /**
     * 가격 비교 Debug 로그 (주문 수정 시)
     */
    public void logPriceComparison(Long productId, int price) {
        log.atDebug()
            .addKeyValue(EVENT, ORDER_REVISED)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(PRODUCT_PRICE, price)
            .log("Comparing product price for order revision");
    }
}
