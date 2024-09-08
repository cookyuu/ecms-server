package com.cookyuu.ecms_server.domain.seller.service;

import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import com.cookyuu.ecms_server.domain.seller.repository.SellerRepository;
import com.cookyuu.ecms_server.global.exception.domain.ECMSSellerException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SellerService {
    private final SellerRepository sellerRepository;

    public Seller findSellerById(Long sellerId) {
        return sellerRepository.findById(sellerId).orElseThrow(ECMSSellerException::new);
    }
}
