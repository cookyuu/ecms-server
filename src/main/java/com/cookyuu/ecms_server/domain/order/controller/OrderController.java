package com.cookyuu.ecms_server.domain.order.controller;

import com.cookyuu.ecms_server.domain.order.dto.*;
import com.cookyuu.ecms_server.domain.order.service.OrderService;
import com.cookyuu.ecms_server.common.utils.UserUtils;
import com.cookyuu.ecms_server.common.web.dto.ApiResponse;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "주문 API", description = "주문 생성, 조회, 취소, 수정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {
    private final OrderService orderService;
    private final UserUtils userUtils;

    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다. 재고 확인 및 비관적 락을 통한 동시성 제어가 적용됩니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "재고 부족")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderDto.Response>> createOrder(@AuthenticationPrincipal UserDetails user, @RequestBody CreateOrderDto.Request orderInfo) {
        CreateOrderDto.Response res = orderService.createOrder(userUtils.getUserId(user), orderInfo);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @Operation(summary = "주문 취소", description = "주문을 취소합니다. 주문한 사용자만 취소할 수 있습니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "취소 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<ResultCode>> cancelOrder(@AuthenticationPrincipal UserDetails user, @RequestBody CancelOrderDto.Request cancelInfo) {
        return ResponseEntity.ok(ApiResponse.success(orderService.cancelOrder(user, cancelInfo)));
    }

    @Operation(summary = "주문 정보 수정", description = "주문 정보를 수정합니다. 배송지 주소 등을 변경할 수 있습니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    @PutMapping
    public ResponseEntity<ApiResponse<ResultCode>> reviseOrderInfo(@AuthenticationPrincipal UserDetails user, @RequestBody ReviseOrderDto.Request reviseInfo) {
        return ResponseEntity.ok(ApiResponse.success(orderService.reviseOrder(user, reviseInfo)));
    }

    @Operation(summary = "주문 목록 검색", description = "주문 목록을 검색 조건에 따라 조회합니다. 페이징을 지원합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SearchOrderDto.Response>>> searchOrderList(
        @Parameter(description = "검색 옵션 (ORDER_NUMBER, MEMBER_NAME 등)")
        @RequestParam(name = "option", required = false) String option,
        @Parameter(description = "검색 키워드")
        @RequestParam(name = "keyword", required = false) String keyword,
        @Parameter(description = "주문 상태 필터 (ALL, PENDING, CONFIRMED, CANCELLED 등)")
        @RequestParam(name = "status", defaultValue = "ALL") String status,
        Pageable pageable) {
        SearchOrderDto.Request req = SearchOrderDto.Request.builder()
                .option(option)
                .keyword(keyword)
                .status(status)
                .pageable(pageable)
                .build();
        Page<SearchOrderDto.Response> resOrderList = orderService.searchOrderList(req);
        return ResponseEntity.ok(ApiResponse.success(resOrderList));
    }

    @Operation(summary = "주문 상세 조회", description = "주문 번호로 주문 상세 정보를 조회합니다. Redis 캐싱이 적용되어 있습니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<OrderDetailDto>> getOrderDetail(
        @AuthenticationPrincipal UserDetails user,
        @Parameter(description = "조회할 주문 번호", required = true)
        @RequestParam(name = "orderNumber") String orderNumber) {
        OrderDetailDto res = orderService.getOrderDetailCacheable(user, orderNumber);
        return ResponseEntity.ok(ApiResponse.success(res));
    }
}
