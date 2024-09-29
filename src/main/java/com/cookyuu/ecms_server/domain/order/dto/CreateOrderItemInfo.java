package com.cookyuu.ecms_server.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CreateOrderItemDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private Long productId;
        private Integer price;
        private Integer quantity;
    }
}
