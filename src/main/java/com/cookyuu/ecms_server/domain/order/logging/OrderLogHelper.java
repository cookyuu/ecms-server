package com.cookyuu.ecms_server.domain.order.logging;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Slf4j
@Component
public class OrderLogHelper {

    public void logStockError(int quantity, int stockQuantity, ResultCode resultCode, String message) {
        log.atError()
            .addKeyValue(EVENT, PRODUCT_OUT_OF_STOCK)
            .addKeyValue(STOCK_QUANTITY, stockQuantity)
            .addKeyValue(QUANTITY, quantity)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .log(message);
    }

    public void logPriceError(Long productId, int orderPrice, Integer currentPrice, ResultCode resultCode, String message) {
        log.atError()
            .addKeyValue(EVENT, VALIDATION_ERROR)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue("order_price", orderPrice)
            .addKeyValue("current_price", currentPrice)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .log(message);
    }

    public void logValidationSuccess(String operation, Long productId, Integer quantity, Integer stockQuantity) {
        log.atDebug()
            .addKeyValue("operation", operation)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(QUANTITY, quantity)
            .addKeyValue(STOCK_QUANTITY, stockQuantity)
            .log("Validation passed");
    }

    public void logStockDecrease(String operation, Long productId, int quantity, Integer stockQuantity) {
        log.atDebug()
            .addKeyValue("operation", operation)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(QUANTITY, quantity)
            .addKeyValue(STOCK_QUANTITY, stockQuantity)
            .log("Stock decreased");
    }

    public void logStockRestore(String operation, Long productId, int quantity, Integer stockQuantity) {
        log.atDebug()
            .addKeyValue("operation", operation)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(QUANTITY, quantity)
            .addKeyValue(STOCK_QUANTITY, stockQuantity)
            .log("Stock restored");
    }

    public void logStockRestoreForRevision(Long productId, int quantity, Integer stockQuantity) {
        log.atDebug()
            .addKeyValue(EVENT, ORDER_REVISED)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(QUANTITY, quantity)
            .addKeyValue(STOCK_QUANTITY, stockQuantity)
            .log("Restored product quantity for order revision");
    }

    public void logStockDecreaseForRevision(Long productId, int quantity, Integer stockQuantity) {
        log.atDebug()
            .addKeyValue(EVENT, ORDER_REVISED)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(QUANTITY, quantity)
            .addKeyValue(STOCK_QUANTITY, stockQuantity)
            .log("Stock decreased for order revision");
    }

    public void logProductNotFoundError(Long userId, Long productId) {
        log.atError()
            .addKeyValue(EVENT, BUSINESS_ERROR)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(ERROR_CODE, ResultCode.PRODUCT_NOT_FOUND.getCode())
            .log("Order creation failed - product not found");
    }

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

    public void logOrderRevisionFailed(Long orderId, String orderStatus, ResultCode resultCode) {
        log.atWarn()
            .addKeyValue(EVENT, BUSINESS_ERROR)
            .addKeyValue(ORDER_ID, orderId)
            .addKeyValue(ORDER_STATUS, orderStatus)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .addKeyValue(ERROR_MESSAGE, "Cannot revise order in current status")
            .log("Order revision failed - invalid status");
    }

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

    public void logOrderDetailFetch(String userRole, String orderNumber) {
        log.atDebug()
            .addKeyValue("operation", "getOrderDetail")
            .addKeyValue(USER_ROLE, userRole)
            .addKeyValue(ORDER_NUMBER, orderNumber)
            .log("Fetching order detail");
    }

    public void logCartUpdate(Long cartId, Long productId) {
        log.atDebug()
            .addKeyValue("operation", "updateCart")
            .addKeyValue(CART_ID, cartId)
            .addKeyValue(PRODUCT_ID, productId)
            .log("Updating cart with ordered items");
    }

    public void logPriceComparison(Long productId, int price) {
        log.atDebug()
            .addKeyValue(EVENT, ORDER_REVISED)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(PRODUCT_PRICE, price)
            .log("Comparing product price for order revision");
    }
}
