package com.cookyuu.ecms_server.domain.order.dto;

import com.cookyuu.ecms_server.domain.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailDto {
    private OrderInfo orderInfo;
    private List<OrderLineInfo> orderLines;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderInfo {
        private String orderNumber;
        private Integer totalPrice;
        private OrderStatus status;
        private String destination;
        private String destinationDetail;
        private String recipientName;
        private String recipientPhoneNumber;
        private Long buyerId;
        private String buyerLoginId;
        private String buyerName;
        private String shipmentNumber;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderLineInfo {
        private Long productId;
        private String productName;
        private Integer quantity;
        private Integer price;
        private Long sellerId;
        private String sellerName;
    }
}
