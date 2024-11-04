package com.cookyuu.ecms_server.domain.product.dto;

import com.cookyuu.ecms_server.global.entity.SortType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

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
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long productId;
        private String name;
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
