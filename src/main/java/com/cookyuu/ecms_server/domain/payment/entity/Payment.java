package com.cookyuu.ecms_server.domain.payment.entity;

import com.cookyuu.ecms_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ecms_payment")
public class Payment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String paymentNumber;
    private String orderNumber;
    private Integer amount;
    private String cancelReason;
    private LocalDateTime canceledAt;

    private Long buyerId;
    private Long sellerId;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;


    private String paymentFailMsg;

    public void cancel(String cancelReason) {
        this.status = PaymentStatus.CANCEL;
        this.cancelReason = cancelReason;
        this.canceledAt = LocalDateTime.now();
    }
}
