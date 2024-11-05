package com.cookyuu.ecms_server.domain.product.repository;

import com.cookyuu.ecms_server.domain.order.dto.OrderDetailDto;
import com.cookyuu.ecms_server.domain.order.dto.SearchOrderDto;
import com.cookyuu.ecms_server.domain.product.dto.SearchProductDto;
import org.springframework.data.domain.Page;

public interface ProductCustomRepository {
    Page<SearchProductDto.Response> searchPageOrderByCreatedAtDesc(SearchProductDto.Request request);
}
