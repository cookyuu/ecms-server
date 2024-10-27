package com.cookyuu.ecms_server.domain.payment.dto;

import com.cookyuu.ecms_server.domain.payment.entity.PaymentMethod;
import com.cookyuu.ecms_server.domain.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetailDto {

    private Long paymentId;
    private String paymentNumber;
    private String orderNumber;
    private Integer amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String cancelReason;
    private LocalDateTime canceledAt;
    private String paymentFailMsg;

    private Long buyerId;
    private Long sellerId;

}
