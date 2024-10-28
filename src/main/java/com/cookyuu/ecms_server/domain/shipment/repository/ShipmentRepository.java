package com.cookyuu.ecms_server.domain.shipment.repository;

import com.cookyuu.ecms_server.domain.shipment.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long>, ShipmentCustomRepository {
    Optional<Shipment> findByShipmentNumber(String shipmentNumber);
}
