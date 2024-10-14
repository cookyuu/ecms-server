package com.cookyuu.ecms_server.domain.shipment.repository;

import com.cookyuu.ecms_server.domain.shipment.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}
