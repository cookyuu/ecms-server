package com.cookyuu.ecms_server.domain.payment.controller;

import com.cookyuu.ecms_server.domain.payment.dto.CancelPaymentDto;
import com.cookyuu.ecms_server.domain.payment.dto.CreatePaymentDto;
import com.cookyuu.ecms_server.domain.payment.service.PaymentService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private final PaymentService paymentService;
    @PostMapping
    public ResponseEntity<ApiResponse<CreatePaymentDto.Response>> createPayment(@AuthenticationPrincipal UserDetails user, @RequestBody CreatePaymentDto.Request paymentInfo){
        CreatePaymentDto.ResponseServ resServ = paymentService.createPayment(user, paymentInfo);
        if (!resServ.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.failure(resServ.getResultCode()));
        }
        return ResponseEntity.ok(ApiResponse.success(CreatePaymentDto.Response.toDto(resServ.getPaymentNumber())));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<CancelPaymentDto.Response>> cancelPayment(@AuthenticationPrincipal UserDetails user, @RequestBody CancelPaymentDto.Request paymentInfo) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.cancelPayment(user, paymentInfo)));

    }

}