package com.cookyuu.ecms_server.domain.shipment.dto;

import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.domain.shipment.entity.Shipment;
import com.cookyuu.ecms_server.domain.shipment.entity.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class CreateShipmentDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String orderNumber;

        public Shipment toEntity(String shipmentNumber, Order order) {
            return Shipment.builder()
                    .shipmentNumber(shipmentNumber)
                    .status(ShipmentStatus.COLLECTION)
                    .order(order)
                    .orderNumber(this.orderNumber)
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Response {
        private String shipmentNumber;
    }
}
