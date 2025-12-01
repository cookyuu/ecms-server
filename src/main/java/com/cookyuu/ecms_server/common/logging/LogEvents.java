package com.cookyuu.ecms_server.common.logging;

/**
 * 표준화된 로그 이벤트 타입 정의
 * Grafana Loki 및 Prometheus 모니터링을 위한 이벤트 상수
 */
public final class LogEvents {

    private LogEvents() {
        throw new AssertionError("Cannot instantiate constants class");
    }

    // ========== Order Events ==========
    public static final String ORDER_CREATED = "ORDER_CREATED";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";
    public static final String ORDER_REVISED = "ORDER_REVISED";
    public static final String ORDER_CONFIRMED = "ORDER_CONFIRMED";

    // ========== Payment Events ==========
    public static final String PAYMENT_REQUESTED = "PAYMENT_REQUESTED";
    public static final String PAYMENT_COMPLETED = "PAYMENT_COMPLETED";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String PAYMENT_CANCELLED = "PAYMENT_CANCELLED";

    // ========== Coupon Events ==========
    public static final String COUPON_CREATED = "COUPON_CREATED";
    public static final String COUPON_ISSUED = "COUPON_ISSUED";
    public static final String COUPON_ISSUE_FAILED = "COUPON_ISSUE_FAILED";
    public static final String COUPON_USED = "COUPON_USED";
    public static final String COUPON_EXPIRED = "COUPON_EXPIRED";

    // ========== Product Events ==========
    public static final String PRODUCT_REGISTERED = "PRODUCT_REGISTERED";
    public static final String PRODUCT_UPDATED = "PRODUCT_UPDATED";
    public static final String PRODUCT_DELETED = "PRODUCT_DELETED";
    public static final String PRODUCT_OUT_OF_STOCK = "PRODUCT_OUT_OF_STOCK";
    public static final String PRODUCT_VIEWED = "PRODUCT_VIEWED";

    // ========== Member Events ==========
    public static final String MEMBER_REGISTERED = "MEMBER_REGISTERED";
    public static final String MEMBER_UPDATED = "MEMBER_UPDATED";
    public static final String MEMBER_DELETED = "MEMBER_DELETED";
    public static final String MEMBER_ROLE_CHANGED = "MEMBER_ROLE_CHANGED";

    // ========== Auth Events ==========
    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String LOGOUT = "LOGOUT";
    public static final String TOKEN_ISSUED = "TOKEN_ISSUED";
    public static final String TOKEN_REFRESHED = "TOKEN_REFRESHED";
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";

    // ========== Cart Events ==========
    public static final String CART_ITEM_ADDED = "CART_ITEM_ADDED";
    public static final String CART_ITEM_REMOVED = "CART_ITEM_REMOVED";
    public static final String CART_ITEM_UPDATED = "CART_ITEM_UPDATED";
    public static final String CART_CLEARED = "CART_CLEARED";

    // ========== Seller Events ==========
    public static final String SELLER_REGISTERED = "SELLER_REGISTERED";
    public static final String SELLER_UPDATED = "SELLER_UPDATED";
    public static final String SELLER_DELETED = "SELLER_DELETED";

    // ========== Shipment Events ==========
    public static final String SHIPMENT_CREATED = "SHIPMENT_CREATED";
    public static final String SHIPMENT_UPDATED = "SHIPMENT_UPDATED";
    public static final String SHIPMENT_DELIVERED = "SHIPMENT_DELIVERED";
    public static final String SHIPMENT_BEGUN = "SHIPMENT_BEGUN";
    public static final String SHIPMENT_LOCATION_UPDATED = "SHIPMENT_LOCATION_UPDATED";

    // ========== Category Events ==========
    public static final String CATEGORY_REGISTERED = "CATEGORY_REGISTERED";
    public static final String CATEGORY_UPDATED = "CATEGORY_UPDATED";
    public static final String CATEGORY_DELETED = "CATEGORY_DELETED";

    // ========== Cart Events (Moved) ==========
    public static final String CART_CREATED = "CART_CREATED";

    // ========== Error Events ==========
    public static final String BUSINESS_ERROR = "BUSINESS_ERROR";
    public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String AUTHENTICATION_ERROR = "AUTHENTICATION_ERROR";
    public static final String AUTHORIZATION_ERROR = "AUTHORIZATION_ERROR";

    // ========== Performance Events ==========
    public static final String SLOW_QUERY = "SLOW_QUERY";
    public static final String LOCK_TIMEOUT = "LOCK_TIMEOUT";
    public static final String OPTIMISTIC_LOCK_FAILURE = "OPTIMISTIC_LOCK_FAILURE";
    public static final String DISTRIBUTED_LOCK_ACQUIRED = "DISTRIBUTED_LOCK_ACQUIRED";
    public static final String DISTRIBUTED_LOCK_FAILED = "DISTRIBUTED_LOCK_FAILED";

    // ========== Cache Events ==========
    public static final String CACHE_HIT = "CACHE_HIT";
    public static final String CACHE_MISS = "CACHE_MISS";
    public static final String CACHE_EVICTED = "CACHE_EVICTED";
}
