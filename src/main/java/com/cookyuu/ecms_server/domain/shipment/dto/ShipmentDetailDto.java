package com.cookyuu.ecms_server.domain.shipment.dto;

import com.cookyuu.ecms_server.domain.shipment.entity.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentDetailDto {
    private Long shipmentId;
    private String shipmentNumber;
    private String currentLocation;
    private ShipmentStatus status;
    private LocalDateTime arrivedAt;
    private String orderNumber;
}
