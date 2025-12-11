package com.cookyuu.ecms_server.domain.payment.controller;

import com.cookyuu.ecms_server.domain.payment.dto.CancelPaymentDto;
import com.cookyuu.ecms_server.domain.payment.dto.CreatePaymentDto;
import com.cookyuu.ecms_server.domain.payment.dto.PaymentDetailDto;
import com.cookyuu.ecms_server.domain.payment.service.PaymentService;
import com.cookyuu.ecms_server.common.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "결제 API", description = "결제 생성, 취소, 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 생성", description = "주문에 대한 결제를 생성합니다. 결제 방법과 금액 정보가 필요합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CreatePaymentDto.Response>> createPayment(@AuthenticationPrincipal UserDetails user, @RequestBody CreatePaymentDto.Request paymentInfo){
        CreatePaymentDto.ResponseServ resServ = paymentService.createPayment(user, paymentInfo);
        if (!resServ.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.failure(resServ.getResultCode()));
        }
        return ResponseEntity.ok(ApiResponse.success(CreatePaymentDto.Response.toDto(resServ.getPaymentNumber())));
    }

    @Operation(summary = "결제 취소", description = "결제를 취소합니다. 결제한 사용자만 취소할 수 있습니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 취소 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "취소 불가능한 상태")
    })
    @DeleteMapping
    public ResponseEntity<ApiResponse<CancelPaymentDto.Response>> cancelPayment(@AuthenticationPrincipal UserDetails user, @RequestBody CancelPaymentDto.Request paymentInfo) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.cancelPayment(user, paymentInfo)));
    }

    @Operation(summary = "결제 상세 조회", description = "결제 번호로 결제 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentDetailDto>>> getPaymentDetail(
        @AuthenticationPrincipal UserDetails user,
        @Parameter(description = "조회할 결제 번호", required = true)
        @RequestParam String paymentNumber) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentDetail(user, paymentNumber)));

    }

}
