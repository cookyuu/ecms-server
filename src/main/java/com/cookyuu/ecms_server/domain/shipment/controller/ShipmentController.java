package com.cookyuu.ecms_server.domain.shipment.controller;

import com.cookyuu.ecms_server.domain.shipment.dto.CreateShipmentDto;
import com.cookyuu.ecms_server.domain.shipment.dto.ShipmentDetailDto;
import com.cookyuu.ecms_server.domain.shipment.dto.UpdateShipmentDto;
import com.cookyuu.ecms_server.domain.shipment.service.ShipmentService;
import com.cookyuu.ecms_server.common.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "배송 API", description = "배송 생성, 조회, 상태 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shipment")
public class ShipmentController {
    private final ShipmentService shipmentService;

    @Operation(summary = "배송 생성", description = "주문에 대한 배송 정보를 생성합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "배송 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CreateShipmentDto.Response>> createShipment(@RequestBody CreateShipmentDto.Request shipmentInfo) {
        CreateShipmentDto.Response res = shipmentService.createShipment(shipmentInfo);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @Operation(summary = "배송 시작", description = "배송을 시작 상태로 변경합니다. 판매자 또는 관리자만 가능합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "배송 시작 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "배송을 찾을 수 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 배송이 시작됨")
    })
    @PutMapping("/begin")
    public ResponseEntity<ApiResponse<Void>> beginShipment(@RequestBody UpdateShipmentDto.Request shipmentInfo) {
        shipmentService.beginShipment(shipmentInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "배송 위치 업데이트", description = "배송 중인 상품의 현재 위치를 업데이트합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "위치 업데이트 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "배송을 찾을 수 없음")
    })
    @PutMapping("/location")
    public ResponseEntity<ApiResponse<Void>> updateLocation(@RequestBody UpdateShipmentDto.Request shipmentInfo) {
        shipmentService.updateLocation(shipmentInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "배송 상세 조회", description = "배송 번호로 배송 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "배송을 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<ShipmentDetailDto>> getShipmentDetail(
        @Parameter(description = "조회할 배송 번호", required = true)
        @RequestParam String shipmentNumber) {
        return ResponseEntity.ok(ApiResponse.success(shipmentService.getShipmentDetail(shipmentNumber)));
    }
}
