package com.cookyuu.ecms_server.domain.order.repository;

import com.cookyuu.ecms_server.domain.order.dto.SearchOrderDto;
import org.springframework.data.domain.Page;

public interface OrderCustomRepository {
    Page<SearchOrderDto.Response> searchPageOrderByCreatedAtDesc(SearchOrderDto.Request request);
}
