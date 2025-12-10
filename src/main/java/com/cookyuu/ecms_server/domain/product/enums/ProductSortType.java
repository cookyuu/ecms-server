package com.cookyuu.ecms_server.domain.product.enums;

import lombok.Getter;

/**
 * 상품 정렬 옵션
 */
@Getter
public enum ProductSortType {
    CREATED_DESC("최신순", "createdAt", "DESC"),
    CREATED_ASC("등록순", "createdAt", "ASC"),
    PRICE_ASC("낮은가격순", "price", "ASC"),
    PRICE_DESC("높은가격순", "price", "DESC"),
    HIT_COUNT_DESC("조회수순", "hitCount", "DESC"),
    POPULAR("인기순", "orderCount", "DESC"); // 주문 수 기준

    private final String description;
    private final String field;
    private final String direction;

    ProductSortType(String description, String field, String direction) {
        this.description = description;
        this.field = field;
        this.direction = direction;
    }
}
