package com.cookyuu.ecms_server.domain.order.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SearchOption {
    ORDER_NUMBER("orderNumber"), LOGIN_ID("loginId"), STATUS("status");
    private final String name;
}
