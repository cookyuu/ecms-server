package com.cookyuu.ecms_server.global.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RedisKeyCode {
    AUTH_EMAIL("auth:email:")
    ,AUTH_PHONE("auth:phone:")
    ,LOGOUT_TOKEN("logout:token:")
    ,REFRESH_TOKEN("refresh:token:")
    ,ORDER_NUMBER("order:number:")
    ,PAYMENT_NUMBER("payment:number:"),

    // COUPON
    COUPON_COUNT_KEY("coupon:count:"),
    COUPON_USER_SET_KEY("coupon:users:"),
    COUPON_QUEUE_KEY("coupon:queue:"),
    DAILY_LIMIT_KEY("coupon:daily:limit:"),
    COUPON_STATUS_KEY("coupon:status:");

    String separator;
}
