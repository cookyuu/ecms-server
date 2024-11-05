package com.cookyuu.ecms_server.domain.product.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SearchOption {
    PRODUCT_NAME("productName"), SELLER_NAME("sellerName"), CATEGORY_NAME("categoryName");
    private final String name;
}