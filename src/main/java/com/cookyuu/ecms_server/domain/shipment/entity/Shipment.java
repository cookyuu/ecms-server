package com.cookyuu.ecms_server.domain.shipment.entity;

import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.entity.BaseTimeEntity;
import com.cookyuu.ecms_server.global.exception.domain.ECMSShipmentException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ecms_shipment")
public class Shipment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String shipmentNumber;
    private String currentLocation;
    private ShipmentStatus status;
    private LocalDateTime arrivedAt;
    private String orderNumber;

    @OneToOne(mappedBy = "shipment")
    private Order order;

    @Builder
     public Shipment (String shipmentNumber, ShipmentStatus status, Order order, String orderNumber) {
         this.shipmentNumber = shipmentNumber;
         this.status = status;
         this.order = order;
         this.orderNumber = orderNumber;
     }

    public void begin(String location) {
        this.currentLocation = location;
        this.status = ShipmentStatus.IN_DELIVERY;
    }

    public void checkStatus(ShipmentStatus status) {
        if (!this.status.equals(status)) {
            throw new ECMSShipmentException(ResultCode.SHIPMENT_STATUS_UNMATCHED);
        }
    }

    public void updateLocation(String location) {
        this.currentLocation = location;
    }
}
