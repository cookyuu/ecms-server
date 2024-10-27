package com.cookyuu.ecms_server.domain.payment.dto;

import com.cookyuu.ecms_server.domain.payment.entity.Payment;
import com.cookyuu.ecms_server.domain.payment.entity.PaymentMethod;
import com.cookyuu.ecms_server.domain.payment.entity.PaymentStatus;
import com.cookyuu.ecms_server.global.code.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CreatePaymentDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private PaymentMethod paymentMethod;
        private Integer paymentPrice;
        private String orderNumber;

        public Payment successPayment(Long buyerId, Long sellerId, String paymentNumber) {
            return Payment.builder()
                    .paymentNumber(paymentNumber)
                    .orderNumber(orderNumber)
                    .amount(this.paymentPrice)
                    .paymentMethod(this.paymentMethod)
                    .status(PaymentStatus.COMPLETE)
                    .buyerId(buyerId)
                    .sellerId(sellerId)
                    .build();
        }

        public Payment failPayment(Long buyerId, Long sellerId, String paymentNumber, String failMsg) {
            return Payment.builder()
                    .paymentNumber(paymentNumber)
                    .orderNumber(orderNumber)
                    .amount(this.paymentPrice)
                    .paymentMethod(this.paymentMethod)
                    .status(PaymentStatus.FAIL)
                    .paymentFailMsg(failMsg)
                    .buyerId(buyerId)
                    .sellerId(sellerId)
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResponseServ {
        private String paymentNumber;
        private boolean isSuccess;
        private ResultCode resultCode;

        public static ResponseServ toDto(String paymentNumber) {
            return ResponseServ.builder()
                    .isSuccess(true)
                    .paymentNumber(paymentNumber)
                    .build();
        }

        public static ResponseServ toDto(String paymentNumber, ResultCode resultCode) {
            return ResponseServ.builder()
                    .paymentNumber(paymentNumber)
                    .isSuccess(false)
                    .resultCode(resultCode)
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private String paymentNumber;
        public static CreatePaymentDto.Response toDto(String paymentNumber) {
            return Response.builder()
                    .paymentNumber(paymentNumber)
                    .build();
        }
    }
}
