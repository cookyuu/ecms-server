package com.cookyuu.ecms_server.domain.product.logging;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Slf4j
@Component
public class ProductLogHelper {

    /**
     * 상품 등록 성공 Info 로그
     */
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

    /**
     * 상품 등록 실패 Error 로그
     */
    public void logProductRegistrationFailed(Long sellerId, ResultCode resultCode, Exception e) {
        log.atError()
            .addKeyValue(EVENT, BUSINESS_ERROR)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .addKeyValue(SELLER_ID, sellerId)
            .setCause(e)
            .log("Product registration failed");
    }

    /**
     * 상품 수정 성공 Info 로그
     */
    public void logProductUpdated(Long productId, Long sellerId) {
        log.atInfo()
            .addKeyValue(EVENT, PRODUCT_UPDATED)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(SELLER_ID, sellerId)
            .log("Product updated successfully");
    }

    /**
     * 상품 삭제 성공 Info 로그
     */
    public void logProductDeleted(Long productId, Long sellerId) {
        log.atInfo()
            .addKeyValue(EVENT, PRODUCT_DELETED)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(SELLER_ID, sellerId)
            .log("Product deleted successfully");
    }

    /**
     * 상품 조회수 증가 Debug 로그
     */
    public void logProductViewed(Long productId) {
        log.atDebug()
            .addKeyValue(EVENT, PRODUCT_VIEWED)
            .addKeyValue(PRODUCT_ID, productId)
            .log("Product hit count increased");
    }

    /**
     * 상품 소유권 확인 Debug 로그
     */
    public void logCheckProductOwnership(Long productId, Long sellerId) {
        log.atDebug()
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(SELLER_ID, sellerId)
            .log("Checking product ownership");
    }

    /**
     * 상품 조회 쿠키 검증 Debug 로그
     */
    public void logValidatePostView(Long productId) {
        log.atDebug()
            .addKeyValue(PRODUCT_ID, productId)
            .log("Validating post view in cookie");
    }
}
