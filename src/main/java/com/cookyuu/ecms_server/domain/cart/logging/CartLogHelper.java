package com.cookyuu.ecms_server.domain.cart.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Slf4j
@Component
public class CartLogHelper {

    public void logCartCreated(Long cartId, Long memberId) {
        log.atInfo()
            .addKeyValue(EVENT, CART_CREATED)
            .addKeyValue(CART_ID, cartId)
            .addKeyValue(MEMBER_ID, memberId)
            .log("Cart created successfully");
    }

    public void logCartItemQuantityValidationFailed(Long cartId, Long productId, int quantity) {
        log.atError()
            .addKeyValue(EVENT, VALIDATION_ERROR)
            .addKeyValue(QUANTITY, quantity)
            .addKeyValue(CART_ID, cartId)
            .addKeyValue(PRODUCT_ID, productId)
            .log("Cart item quantity must be at least 1");
    }

    public void logCartItemUpdated(Long cartId, Long cartItemId, Long productId, int quantity) {
        log.atInfo()
            .addKeyValue(EVENT, CART_ITEM_UPDATED)
            .addKeyValue(CART_ID, cartId)
            .addKeyValue(CART_ITEM_ID, cartItemId)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(QUANTITY, quantity)
            .log("Cart item quantity updated");
    }

    public void logCartItemAdded(Long cartId, Long cartItemId, Long productId, int quantity) {
        log.atInfo()
            .addKeyValue(EVENT, CART_ITEM_ADDED)
            .addKeyValue(CART_ID, cartId)
            .addKeyValue(CART_ITEM_ID, cartItemId)
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(QUANTITY, quantity)
            .log("Cart item added successfully");
    }

    public void logCartItemRemoved(Long cartId, Long cartItemId, Long productId) {
        log.atInfo()
            .addKeyValue(EVENT, CART_ITEM_REMOVED)
            .addKeyValue(CART_ID, cartId)
            .addKeyValue(CART_ITEM_ID, cartItemId)
            .addKeyValue(PRODUCT_ID, productId)
            .log("Cart item removed successfully");
    }
}
