package com.cookyuu.ecms_server.domain.shipment.service;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.domain.order.service.OrderService;
import com.cookyuu.ecms_server.domain.shipment.dto.CreateShipmentDto;
import com.cookyuu.ecms_server.domain.shipment.dto.ShipmentDetailDto;
import com.cookyuu.ecms_server.domain.shipment.dto.UpdateShipmentDto;
import com.cookyuu.ecms_server.domain.shipment.entity.Shipment;
import com.cookyuu.ecms_server.domain.shipment.enums.ShipmentStatus;
import com.cookyuu.ecms_server.domain.shipment.repository.ShipmentRepository;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.generator.BusinessNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final OrderService orderService;
    private final BusinessNumberGenerator businessNumberGenerator;

    @Transactional
    public CreateShipmentDto.Response createShipment(CreateShipmentDto.Request shipmentInfo) {
        Order order = orderService.findOrderByOrderNumber(shipmentInfo.getOrderNumber());
        order.validatePaymentComplete();
        String shipmentNumber = businessNumberGenerator.generateShipmentNumber();

        Shipment shipment = shipmentInfo.toEntity(shipmentNumber, order);
        try {
            shipmentRepository.save(shipment);
            order.successShipment(shipment);
            log.atInfo()
                    .addKeyValue(EVENT, SHIPMENT_CREATED)
                    .addKeyValue(SHIPMENT_NUMBER, shipmentNumber)
                    .addKeyValue(ORDER_NUMBER, shipmentInfo.getOrderNumber())
                    .log("Shipment created successfully");
        } catch (Exception e) {
            log.atError()
                    .addKeyValue(EVENT, SYSTEM_ERROR)
                    .addKeyValue(ORDER_NUMBER, shipmentInfo.getOrderNumber())
                    .log("Failed to create shipment", e);
            throw e;
        }

        return CreateShipmentDto.Response.builder()
                .shipmentNumber(shipmentNumber)
                .build();
    }

    @Transactional
    public void beginShipment(UpdateShipmentDto.Request shipmentInfo) {
        Shipment shipment = findShipmentByShipmentNumber(shipmentInfo.getShipmentNumber());
        shipment.checkStatus(ShipmentStatus.COLLECTION);
        shipment.begin(shipmentInfo.getLocation());
        log.atInfo()
                .addKeyValue(EVENT, SHIPMENT_BEGUN)
                .addKeyValue(SHIPMENT_NUMBER, shipmentInfo.getShipmentNumber())
                .addKeyValue(LOCATION, shipmentInfo.getLocation())
                .log("Shipment delivery begun");
    }

    @Transactional
    public void updateLocation(UpdateShipmentDto.Request shipmentInfo) {
        Shipment shipment = findShipmentByShipmentNumber(shipmentInfo.getShipmentNumber());
        shipment.checkStatus(ShipmentStatus.IN_DELIVERY);
        shipment.updateLocation(shipmentInfo.getLocation());
        log.atInfo()
                .addKeyValue(EVENT, SHIPMENT_LOCATION_UPDATED)
                .addKeyValue(SHIPMENT_NUMBER, shipmentInfo.getShipmentNumber())
                .addKeyValue(LOCATION, shipmentInfo.getLocation())
                .log("Shipment location updated");
    }

    @Transactional(readOnly = true)
    public ShipmentDetailDto getShipmentDetail(String shipmentNumber) {
        return shipmentRepository.getShipmentDetail(shipmentNumber);
    }

    private Shipment findShipmentByShipmentNumber(String shipmentNumber) {
        return shipmentRepository.findByShipmentNumber(shipmentNumber).orElseThrow(() -> new BusinessException(ResultCode.SHIPMENT_NOT_FOUND));
    }
}
