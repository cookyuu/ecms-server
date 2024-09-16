package com.cookyuu.ecms_server.domain.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UpdateCartItemDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        @NotNull
        private Long productId;

        @NotNull
        private Integer quantity;
    }
}
