package com.cookyuu.ecms_server.domain.seller.mapper;

import com.cookyuu.ecms_server.domain.member.entity.RoleType;
import com.cookyuu.ecms_server.domain.seller.dto.RegisterSellerDto;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;

public class SellerRegistrationMapper {
    public static Seller toEntity(RegisterSellerDto.Request sellerInfo, String encryptPw) {
        return Seller.builder()
                .loginId(sellerInfo.getLoginId())
                .password(encryptPw)
                .name(sellerInfo.getName())
                .businessName(sellerInfo.getBusinessName())
                .businessNumber(sellerInfo.getBusinessNumber())
                .businessAddress(sellerInfo.getBusinessAddress())
                .businessContactTelNum(sellerInfo.getBusinessContactTelNum())
                .businessContactEmail(sellerInfo.getBusinessContactEmail())
                .role(RoleType.SELLER)
                .build();
    }

    public static RegisterSellerDto.Response toDto(Seller seller) {
        return RegisterSellerDto.Response.builder().sellerId(seller.getId()).build();
    }
}
