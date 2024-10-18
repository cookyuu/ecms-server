package com.cookyuu.ecms_server.domain.order.dto;

import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.domain.order.entity.OrderStatus;
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
        @NotNull
        private OrderShipmentInfo shipmentInfo;
        private int totalPrice;
        private String orderNumber;
        private Member buyer;

        @Builder
        public Request (List<CreateOrderItemInfo> orderItemList, OrderShipmentInfo shipmentInfo) {
            this.orderItemList = orderItemList;
            this.shipmentInfo = shipmentInfo;
        }

        public void addTotalPrice (int totalPrice) {
            this.totalPrice = totalPrice;
        }
        public void addOrderNumber(String orderNumber) {this.orderNumber = orderNumber; }
        public void addBuyer(Member buyer) {this.buyer = buyer; }

        public Order toEntity() {
            return Order.builder()
                    .totalPrice(this.totalPrice)
                    .orderNumber(this.orderNumber)
                    .status(OrderStatus.ORDER_COMPLETE)
                    .buyer(this.buyer)
                    .destination(this.shipmentInfo.getDestination())
                    .destinationDetail(this.shipmentInfo.getDestinationDetail())
                    .recipientName(this.shipmentInfo.getRecipientName())
                    .recipientPhoneNumber(this.shipmentInfo.getRecipientPhoneNumber())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private String orderNumber;

        public static CreateOrderDto.Response toDto(Order order) {
            return CreateOrderDto.Response.builder()
                    .orderNumber(order.getOrderNumber())
                    .build();
        }
    }
}
