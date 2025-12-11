package com.cookyuu.ecms_server.common.logging;

public final class LogFields {

    private LogFields() {
        throw new AssertionError("Cannot instantiate constants class");
    }

    // ========== Common Fields ==========
    public static final String EVENT = "event";
    public static final String STATUS = "status";
    public static final String ERROR_CODE = "error_code";
    public static final String ERROR_MESSAGE = "error_message";
    public static final String DURATION_MS = "duration_ms";
    public static final String TIMESTAMP = "timestamp";

    // ========== User/Member Fields ==========
    public static final String USER_ID = "user_id";
    public static final String USER_ROLE = "user_role";
    public static final String LOGIN_ID = "login_id";
    public static final String EMAIL = "email";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String MEMBER_ID = "member_id";

    // ========== Order Fields ==========
    public static final String ORDER_ID = "order_id";
    public static final String ORDER_NUMBER = "order_number";
    public static final String ORDER_STATUS = "order_status";
    public static final String TOTAL_AMOUNT = "total_amount";
    public static final String ITEM_COUNT = "item_count";
    public static final String CANCEL_REASON = "cancel_reason";

    // ========== Payment Fields ==========
    public static final String PAYMENT_ID = "payment_id";
    public static final String PAYMENT_NUMBER = "payment_number";
    public static final String PAYMENT_METHOD = "payment_method";
    public static final String PAYMENT_AMOUNT = "payment_amount";
    public static final String PAYMENT_STATUS = "payment_status";
    public static final String REQUESTED_AMOUNT = "requested_amount";
    public static final String EXPECTED_AMOUNT = "expected_amount";

    // ========== Product Fields ==========
    public static final String PRODUCT_ID = "product_id";
    public static final String PRODUCT_NAME = "product_name";
    public static final String PRODUCT_PRICE = "price";
    public static final String STOCK_QUANTITY = "stock_quantity";
    public static final String CATEGORY = "category";
    public static final String SELLER_ID = "seller_id";

    // ========== Coupon Fields ==========
    public static final String COUPON_ID = "coupon_id";
    public static final String COUPON_NUMBER = "coupon_number";
    public static final String COUPON_TYPE = "coupon_type";
    public static final String COUPON_CODE = "coupon_code";
    public static final String DISCOUNT_PRICE = "discount_price";

    // ========== Cart Fields ==========
    public static final String CART_ID = "cart_id";
    public static final String CART_ITEM_ID = "cart_item_id";
    public static final String QUANTITY = "quantity";

    // ========== Shipment Fields ==========
    public static final String SHIPMENT_ID = "shipment_id";
    public static final String SHIPMENT_NUMBER = "shipment_number";
    public static final String SHIPMENT_STATUS = "shipment_status";
    public static final String LOCATION = "location";

    // ========== Seller Fields ==========
    public static final String BUSINESS_NUMBER = "business_number";
    public static final String BUSINESS_NAME = "business_name";

    // ========== Category Fields ==========
    public static final String CATEGORY_ID = "category_id";
    public static final String CATEGORY_NAME = "category_name";
    public static final String PARENT_CATEGORY_NAME = "parent_category_name";

    // ========== Auth Fields ==========
    public static final String TOKEN_TYPE = "token_type";
    public static final String IP_ADDRESS = "ip_address";
    public static final String USER_AGENT = "user_agent";

    // ========== Performance Fields ==========
    public static final String OPERATION = "operation";
    public static final String QUERY_TIME_MS = "query_time_ms";
    public static final String LOCK_WAIT_MS = "lock_wait_ms";
    public static final String LOCK_KEY = "lock_key";

    // ========== Cache Fields ==========
    public static final String CACHE_NAME = "cache_name";
    public static final String CACHE_KEY = "cache_key";

    // ========== HTTP Fields ==========
    public static final String HTTP_METHOD = "http_method";
    public static final String HTTP_PATH = "http_path";
    public static final String HTTP_STATUS = "http_status";
    public static final String REQUEST_ID = "request_id";
    public static final String TRACE_ID = "trace_id";
}
