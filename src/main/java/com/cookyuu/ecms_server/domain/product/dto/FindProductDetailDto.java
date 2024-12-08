package com.cookyuu.ecms_server.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FindProductDetailDto {
    private Long productId;
    private String productName;
    private String productDescription;
    private Integer productPrice;
    private Integer productStockQuantity;
    private Integer productHitCount;
    private boolean isDeleted;
    private String categoryName;
    private Long sellerId;
    private String sellerName;
}
