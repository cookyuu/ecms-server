package com.cookyuu.ecms_server.domain.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SellerDetailDto {
    private Long sellerId;
    private String name;
    private String businessName;
    private String businessNumber;
    private String businessAddress;
    private String businessContactTelNum;
    private String businessContactEmail;
    private String loginId;
}
