package com.cookyuu.ecms_server.domain.order.dto;

import com.cookyuu.ecms_server.domain.member.entity.Member;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ReviseOrderDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        @NotNull
        private String orderNumber;
        @NotNull
        private List<ReviseOrderItemInfo> orderItemList;
        private int totalPrice;

        public void addTotalPrice (int totalPrice) {
            this.totalPrice = totalPrice;
        }
    }
}
