package com.cookyuu.ecms_server.domain.product.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SearchOption {

    PRODUCT_NAME("productName"), CATEGORY_NAME("categoryName"), SELLER_NAME("sellerName");
    private final String name;
}

