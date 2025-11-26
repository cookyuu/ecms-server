package com.cookyuu.ecms_server.domain.order.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderSearchOption {
    ORDER_NUMBER("orderNumber"), LOGIN_ID("loginId"), STATUS("status");
    private final String name;
}
