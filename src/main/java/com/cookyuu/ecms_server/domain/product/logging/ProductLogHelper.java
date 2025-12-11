package com.cookyuu.ecms_server.domain.product.logging;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Slf4j
@Component
public class ProductLogHelper {

    public void logProductRegistered(Long productId, String productName, Long sellerId,
                                     String category, Integer price, Integer stockQuantity) {
        log.atInfo()
            .addKeyValue(EVENT, PRODUCT_REGISTERED)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(PRODUCT_NAME, productName)
            .addKeyValue(SELLER_ID, sellerId)
            .addKeyValue(CATEGORY, category)
            .addKeyValue(PRODUCT_PRICE, price)
            .addKeyValue(STOCK_QUANTITY, stockQuantity)
            .log("Product registered successfully");
    }

    public void logProductRegistrationFailed(Long sellerId, ResultCode resultCode, Exception e) {
        log.atError()
            .addKeyValue(EVENT, BUSINESS_ERROR)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .addKeyValue(SELLER_ID, sellerId)
            .setCause(e)
            .log("Product registration failed");
    }

    public void logProductUpdated(Long productId, Long sellerId) {
        log.atInfo()
            .addKeyValue(EVENT, PRODUCT_UPDATED)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(SELLER_ID, sellerId)
            .log("Product updated successfully");
    }

    public void logProductDeleted(Long productId, Long sellerId) {
        log.atInfo()
            .addKeyValue(EVENT, PRODUCT_DELETED)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(SELLER_ID, sellerId)
            .log("Product deleted successfully");
    }

    public void logProductViewed(Long productId) {
        log.atDebug()
            .addKeyValue(EVENT, PRODUCT_VIEWED)
            .addKeyValue(PRODUCT_ID, productId)
            .log("Product hit count increased");
    }

    public void logCheckProductOwnership(Long productId, Long sellerId) {
        log.atDebug()
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(SELLER_ID, sellerId)
            .log("Checking product ownership");
    }

    public void logValidatePostView(Long productId) {
        log.atDebug()
            .addKeyValue(PRODUCT_ID, productId)
            .log("Validating post view in cookie");
    }
}
