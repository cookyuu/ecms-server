package com.cookyuu.ecms_server.domain.product.dto;

import com.cookyuu.ecms_server.common.enums.SortType;
import com.cookyuu.ecms_server.domain.product.enums.ProductSortType;
import com.cookyuu.ecms_server.domain.product.enums.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public class SearchProductDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        // 기존 검색 옵션
        private String option;
        private String keyword;
        private SortType sortType;
        private Pageable pageable;

        // 고급 필터 옵션
        private Integer minPrice;              // 최소 가격
        private Integer maxPrice;              // 최대 가격
        private StockStatus stockStatus;       // 재고 상태
        private List<Long> categoryIds;        // 카테고리 ID 리스트 (다중 선택)
        private ProductSortType productSortType; // 확장 정렬 옵션
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long productId;
        private String productName;
        private String description;
        private Integer price;
        private Integer stockQuantity;
        private LocalDateTime modifiedAt;
        private boolean isDeleted;
        private LocalDateTime deletedAt;
        private Long categoryId;
        private String categoryName;
        private Long sellerId;
        private String sellerName;
    }



}
