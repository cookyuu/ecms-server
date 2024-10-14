package com.cookyuu.ecms_server.domain.shipment.controller;

import com.cookyuu.ecms_server.domain.shipment.dto.CreateShipmentDto;
import com.cookyuu.ecms_server.domain.shipment.service.ShipmentService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shipment")
public class ShipmentController {
    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateShipmentDto.Response>> createShipment(@RequestBody CreateShipmentDto.Request shipmentInfo) {
        CreateShipmentDto.Response res = shipmentService.createShipment(shipmentInfo);
        return ResponseEntity.ok(ApiResponse.success(res));
    }
}
