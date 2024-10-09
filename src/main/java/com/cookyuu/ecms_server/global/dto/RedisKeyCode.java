package com.cookyuu.ecms_server.global.dto;

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
    ,PAYMENT_NUMBER("payment:number:");

    String separator;
}
