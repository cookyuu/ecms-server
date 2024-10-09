package com.cookyuu.ecms_server.domain.payment.dto;

import com.cookyuu.ecms_server.domain.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class CancelPaymentDto {
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String paymentNumber;
        private String orderNumber;
        private String cancelReason;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private String paymentNumber;
        private LocalDateTime cancelDateTime;

        public static Response toDto(Payment payment) {
            return Response.builder()
                    .paymentNumber(payment.getPaymentNumber())
                    .cancelDateTime(payment.getCanceledAt())
                    .build();
        }
    }
}
