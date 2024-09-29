package com.cookyuu.ecms_server.domain.order.dto;

import lombok.Getter;

@Getter
public enum OrderNumberCode {
    // ORDER
    NORMAL_ORDER("N"),

    // COOPON
    NO_COOPON("N");
    private String code;

    OrderNumberCode(String code) {
        this.code = code;
    }
}
