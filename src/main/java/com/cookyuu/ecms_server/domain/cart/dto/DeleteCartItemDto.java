package com.cookyuu.ecms_server.domain.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class DeleteCartItemDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private Long productId;
    }
}
