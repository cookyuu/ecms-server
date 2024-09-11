package com.cookyuu.ecms_server.domain.seller.dto;

import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class RegisterSellerDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String name;
        private String businessName;
        private String businessNumber;
        private String businessAddress;
        private String businessContactTelNum;
        private String businessContactEmail;

        public Seller toEntity() {
            return Seller.builder()
                    .name(this.name)
                    .businessName(this.businessName)
                    .businessNumber(this.businessNumber)
                    .businessAddress(this.businessAddress)
                    .businessContactTelNum(this.businessContactTelNum)
                    .businessContactEmail(this.businessContactEmail)
                    .build();
        }
    }
}
