package com.cookyuu.ecms_server.domain.order.dto;

import lombok.Getter;

@Getter
public enum OrderCode {
    // ORDER
    NORMAL_ORDER("N"),

    // COOPON
    NO_COOPON("N");
    private String code;

    OrderCode(String code) {
        this.code = code;
    }
}
