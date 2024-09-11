package com.cookyuu.ecms_server.domain.seller.service;

import com.cookyuu.ecms_server.domain.seller.dto.RegisterSellerDto;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import com.cookyuu.ecms_server.domain.seller.repository.SellerRepository;
import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.domain.ECMSSellerException;
import com.cookyuu.ecms_server.global.utils.ValidateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepository sellerRepository;
    private final ValidateUtils validateUtils;

    public Seller findSellerById(Long sellerId) {
        return sellerRepository.findById(sellerId).orElseThrow(ECMSSellerException::new);
    }

    @Transactional
    public void registerSeller (RegisterSellerDto.Request sellerInfo) {
        validateRegistrationSeller(sellerInfo);
        Seller seller = sellerInfo.toEntity();
        sellerRepository.save(seller);
        log.info("[RegisterSeller] Register seller, OK!!");
    }

    private void validateRegistrationSeller(RegisterSellerDto.Request sellerInfo){
        if (sellerRepository.existsByBusinessNumber(sellerInfo.getBusinessNumber())) {
            throw new ECMSSellerException(ResultCode.VALID_BUSINESSNUM_DUPLICATE);
        }
        validateUtils.isAvailableBusinessNumber(sellerInfo.getBusinessNumber());
        validateUtils.isAvailablePhoneNumberFormat(sellerInfo.getBusinessContactTelNum());
        validateUtils.isAvailableEmailFormat(sellerInfo.getBusinessContactEmail());

        log.info("[RegisterSeller] Validate seller info for seller registration, OK!!");
    }
}
