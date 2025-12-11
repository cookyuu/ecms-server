package com.cookyuu.ecms_server.domain.product.enums;

import lombok.Getter;

@Getter
public enum StockStatus {
    ALL("전체", null),           // 전체 조회
    IN_STOCK("재고있음", true),   // 재고 있음 (stockQuantity > 0)
    OUT_OF_STOCK("품절", false); // 품절 (stockQuantity = 0)

    private final String description;
    private final Boolean hasStock;

    StockStatus(String description, Boolean hasStock) {
        this.description = description;
        this.hasStock = hasStock;
    }
}
