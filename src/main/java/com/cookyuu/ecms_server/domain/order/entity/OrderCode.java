package com.cookyuu.ecms_server.domain.order.entity;

import lombok.Getter;

@Getter
public enum OrderCode {
    // ORDER
    NORMAL_ORDER("N");

    private String code;

    OrderCode(String code) {
        this.code = code;
    }
}
