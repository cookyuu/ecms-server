package com.cookyuu.ecms_server.domain.seller.dto;

import com.cookyuu.ecms_server.domain.member.entity.RoleType;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class RegisterSellerDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String loginId;
        @NotNull
        private String password;
        private String name;
        private String businessName;
        private String businessNumber;
        private String businessAddress;
        private String businessContactTelNum;
        private String businessContactEmail;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long sellerId;
    }
}
