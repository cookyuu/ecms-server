package com.cookyuu.ecms_server.domain.shipment.service;

import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.domain.order.service.OrderService;
import com.cookyuu.ecms_server.domain.shipment.dto.CreateShipmentDto;
import com.cookyuu.ecms_server.domain.shipment.dto.UpdateShipmentDto;
import com.cookyuu.ecms_server.domain.shipment.entity.Shipment;
import com.cookyuu.ecms_server.domain.shipment.entity.ShipmentStatus;
import com.cookyuu.ecms_server.domain.shipment.repository.ShipmentRepository;
import com.cookyuu.ecms_server.global.exception.domain.ECMSShipmentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final OrderService orderService;

    @Transactional
    public CreateShipmentDto.Response createShipment(CreateShipmentDto.Request shipmentInfo) {
        Order order = orderService.findOrderByOrderNumber(shipmentInfo.getOrderNumber());
        order.isPaymentComplete();
        String shipmentNumber = createShipmentNumber();

        Shipment shipment = shipmentInfo.toEntity(shipmentNumber, order);
        try {
            shipmentRepository.save(shipment);
            order.successShipment(shipment);
        } catch (Exception e) {
            log.error("[Shipment::ERROR] Create shipment, Fail..");
            throw e;
        }

        log.debug("[Shipment::Create] Create shipment, OK.");
        return CreateShipmentDto.Response.builder()
                .shipmentNumber(shipmentNumber)
                .build();
    }

    @Transactional
    public void beginShipment(UpdateShipmentDto.Request shipmentInfo) {
        Shipment shipment = findShipmentByShipmentNumber(shipmentInfo.getShipmentNumber());
        shipment.checkStatus(ShipmentStatus.COLLECTION);
        shipment.begin(shipmentInfo.getLocation());
    }

    @Transactional
    public void updateLocation(UpdateShipmentDto.Request shipmentInfo) {
        Shipment shipment = findShipmentByShipmentNumber(shipmentInfo.getShipmentNumber());
        shipment.checkStatus(ShipmentStatus.IN_DELIVERY);
        shipment.updateLocation(shipmentInfo.getLocation());
    }

    private Shipment findShipmentByShipmentNumber(String shipmentNumber) {
        return shipmentRepository.findByShipmentNumber(shipmentNumber).orElseThrow(ECMSShipmentException::new);
    }

    private String createShipmentNumber() {
        StringBuilder sb = new StringBuilder();
        String formatDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
        sb.append("SP").append(formatDate);
        for (int i = 0; i < 5; i++) {
            int random = (int) (Math.random() * 10);
            sb.append(random);
        }
        String shipmentNumber = sb.toString();
        log.debug("[Shipment::Create] Create shipment number, OK. shipmentNumber : {}", shipmentNumber);
        return shipmentNumber;
    }
}
