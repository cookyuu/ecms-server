package com.cookyuu.ecms_server.domain.order.dto;

import com.cookyuu.ecms_server.domain.order.entity.OrderStatus;
import com.cookyuu.ecms_server.global.entity.SortType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

public class SearchOrderDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String option;
        private String keyword;
        private String status;
        private SortType sortType;
        private Pageable pageable;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long orderId;
        private Integer totalPrice;
        private String orderNumber;
        private OrderStatus status;
        private String destination;
        private String destinationDetail;
        private String recipientName;
        private String recipientPhoneNumber;
        private String buyerLoginId;
        private String shipmentNumber;
    }



}
