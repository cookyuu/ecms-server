package com.cookyuu.ecms_server.domain.seller.repository;

import com.cookyuu.ecms_server.domain.seller.dto.SellerDetailDto;

public interface SellerCustomRepository {
    SellerDetailDto getSellerDetail(Long id);
}
