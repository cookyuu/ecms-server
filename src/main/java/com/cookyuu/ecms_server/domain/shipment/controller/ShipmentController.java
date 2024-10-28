package com.cookyuu.ecms_server.domain.shipment.controller;

import com.cookyuu.ecms_server.domain.shipment.dto.CreateShipmentDto;
import com.cookyuu.ecms_server.domain.shipment.dto.ShipmentDetailDto;
import com.cookyuu.ecms_server.domain.shipment.dto.UpdateShipmentDto;
import com.cookyuu.ecms_server.domain.shipment.service.ShipmentService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/begin")
    public ResponseEntity<ApiResponse<Void>> beginShipment(@RequestBody UpdateShipmentDto.Request shipmentInfo) {
        shipmentService.beginShipment(shipmentInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PutMapping("/location")
    public ResponseEntity<ApiResponse<Void>> updateLocation(@RequestBody UpdateShipmentDto.Request shipmentInfo) {
        shipmentService.updateLocation(shipmentInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ShipmentDetailDto>> getShipmentDetail(@RequestParam String shipmentNumber) {
        return ResponseEntity.ok(ApiResponse.success(shipmentService.getShipmentDetail(shipmentNumber)));
    }
}
