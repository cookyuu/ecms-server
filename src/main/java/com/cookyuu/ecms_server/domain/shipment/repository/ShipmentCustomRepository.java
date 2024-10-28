package com.cookyuu.ecms_server.domain.shipment.repository;

import com.cookyuu.ecms_server.domain.shipment.dto.ShipmentDetailDto;

public interface ShipmentCustomRepository {
    ShipmentDetailDto getShipmentDetail(String shipmentNumber);
}
