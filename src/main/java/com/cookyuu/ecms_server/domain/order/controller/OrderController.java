package com.cookyuu.ecms_server.domain.order.controller;

import com.cookyuu.ecms_server.domain.order.dto.*;
import com.cookyuu.ecms_server.domain.order.service.OrderService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import com.cookyuu.ecms_server.global.code.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<CreateOrderDto.Response>> createOrder(@AuthenticationPrincipal UserDetails user, @RequestBody CreateOrderDto.Request orderInfo) {
        CreateOrderDto.Response res = orderService.createOrder(Long.parseLong(user.getUsername()), orderInfo);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PostMapping("/cancel")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<ResultCode>> cancelOrder(@AuthenticationPrincipal UserDetails user, @RequestBody CancelOrderDto.Request cancelInfo) {
        return ResponseEntity.ok(ApiResponse.success(orderService.cancelOrder(user, cancelInfo)));
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<ResultCode>> reviseOrderInfo(@AuthenticationPrincipal UserDetails user, @RequestBody ReviseOrderDto.Request reviseInfo) {
        return ResponseEntity.ok(ApiResponse.success(orderService.reviseOrder(user, reviseInfo)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SearchOrderDto.Response>>> searchOrderList(@RequestParam(name = "option", required = false) String option,
                                                                                      @RequestParam(name = "keyword", required = false) String keyword,
                                                                                      Pageable pageable) {
        SearchOrderDto.Request req = SearchOrderDto.Request.builder()
                .option(option)
                .keyword(keyword)
                .pageable(pageable)
                .build();
        Page<SearchOrderDto.Response> resOrderList = orderService.searchOrderList(req);
        return ResponseEntity.ok(ApiResponse.success(resOrderList));
    }

    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_SELLER','ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<OrderDetailDto>> getOrderDetail(@AuthenticationPrincipal UserDetails user, @RequestParam(name = "orderNumber") String orderNumber) {
        OrderDetailDto res = orderService.getOrderDetail(user, orderNumber);
        return ResponseEntity.ok(ApiResponse.success(res));
    }
}
