package com.cookyuu.ecms_server.domain.shipment.entity;

import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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
    private LocalDateTime shippedAt;
    private LocalDateTime arrivedAt;

    @OneToOne(mappedBy = "shipment")
    private Order order;
}
