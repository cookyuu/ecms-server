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
        private String option;
        private String keyword;
        private SortType sortType;
        private Pageable pageable;

        private Integer minPrice;
        private Integer maxPrice;
        private StockStatus stockStatus;
        private List<Long> categoryIds;
        private ProductSortType productSortType;
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
