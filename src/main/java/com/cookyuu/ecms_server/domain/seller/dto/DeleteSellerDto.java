package com.cookyuu.ecms_server.domain.seller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class DeleteSellerDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        @NotNull
        private String password;
        @NotNull
        private String confirmPassword;
    }
}