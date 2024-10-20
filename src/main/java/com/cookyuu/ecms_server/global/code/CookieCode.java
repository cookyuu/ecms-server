package com.cookyuu.ecms_server.global.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CookieCode {
    REFRESH_TOKEN("refresh_token");
    private final String key;
}
