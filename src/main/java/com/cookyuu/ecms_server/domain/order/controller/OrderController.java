package com.cookyuu.ecms_server.domain.order.controller;

import com.cookyuu.ecms_server.domain.order.dto.CancelOrderDto;
import com.cookyuu.ecms_server.domain.order.dto.CreateOrderDto;
import com.cookyuu.ecms_server.domain.order.service.OrderService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import com.cookyuu.ecms_server.global.dto.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {
    private final OrderService orderService;

    /*
    * 1. 주문 생성
    *   - 장바구니 내용 바탕으로 초기화
    *   - 사용자 정보 가져와 배송정보, 연락처 미리 가져오기
    *   - 고유한 주문 ID 생성
    * 2. 주문 검증
    *   - 상품 재고 확인
    *   - 상품 가격 확인 (요청 상품 결제금액이랑 현재 상품 금액이 차이가 발생할시 알려줘야함)
    *   - 쿠폰 할인코드 유효성 검증
    * 3. 배송 정보 입력
    *   - 사용자로부터 배송지 정보 받아서 입력 (주소 유효성 검증)
    *   - 배송비 확인 (지역별 배송비, 무료 배송 여부 확인)
    * 4. 결제
    * 5. 주문확정
    * 6. 배송준비 및 관리
    * 7. 주문상태 업데이트
    * 8. 반품 및 교환
    */

    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderDto.Response>> createOrder(@AuthenticationPrincipal UserDetails user, @RequestBody CreateOrderDto.Request orderInfo) {
        CreateOrderDto.Response res = orderService.createOrder(user, orderInfo);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<ResultCode>> cancelOrder(@AuthenticationPrincipal UserDetails user, @RequestBody CancelOrderDto.Request cancelInfo) {
        return ResponseEntity.ok(ApiResponse.success(orderService.cancelOrder(user, cancelInfo)));
    }
}
