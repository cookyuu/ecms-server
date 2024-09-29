package com.cookyuu.ecms_server.domain.order.dto;

import com.cookyuu.ecms_server.domain.member.entity.Member;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class CreateOrderDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        @NotNull
        private List<CreateOrderItemInfo> orderItemList;
        private int totalPrice;
        private String orderNumber;
        private Member buyer;

        public void addTotalPrice (int totalPrice) {
            this.totalPrice = totalPrice;
        }
        public void addOrderNumber(String orderNumber) {this.orderNumber = orderNumber; }
        public void addBuyer(Member buyer) {this.buyer = buyer; }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private String orderNumber;
    }
}
