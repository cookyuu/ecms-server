package com.cookyuu.ecms_server.domain.product.repository;

import com.cookyuu.ecms_server.domain.product.dto.FindProductDetailDto;
import com.cookyuu.ecms_server.domain.product.dto.SearchProductDto;
import org.springframework.data.domain.Page;

public interface ProductCustomRepository {
    Page<SearchProductDto.Response> searchPageOrderByCreatedAtDesc(SearchProductDto.Request request);
    FindProductDetailDto findProductDetail(Long productId);
}
