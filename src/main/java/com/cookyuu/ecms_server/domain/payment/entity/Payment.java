package com.cookyuu.ecms_server.domain.payment.entity;

import com.cookyuu.ecms_server.common.domain.BaseTimeEntity;
import com.cookyuu.ecms_server.domain.payment.enums.PaymentMethod;
import com.cookyuu.ecms_server.domain.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 결제 엔티티
 *
 * 참고: 현재는 Long buyerId, sellerId로 관리
 * 향후 MSA 분리 또는 별도 DB 사용 시를 고려한 설계
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "ecms_payment",
        indexes = {
                @Index(name = "ecms_payment_idx_1", columnList = "paymentNumber", unique = true),
                @Index(name = "ecms_payment_idx_2", columnList = "orderNumber"),
                @Index(name = "ecms_payment_idx_3", columnList = "buyerId"),
                @Index(name = "ecms_payment_idx_4", columnList = "status")
        }
)
public class Payment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 결제 번호 (고유)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String paymentNumber;

    /**
     * 주문 번호 (참조용)
     */
    @Column(nullable = false, length = 50)
    private String orderNumber;

    /**
     * 결제 금액
     */
    @Column(nullable = false)
    private Integer amount;

    /**
     * 취소 사유
     */
    @Column(length = 500)
    private String cancelReason;

    /**
     * 취소 일시
     */
    private LocalDateTime canceledAt;

    /**
     * 구매자 ID (FK 대신 Long 사용)
     * MSA 환경 또는 Payment 서비스 분리 시를 고려
     */
    @Column(nullable = false)
    private Long buyerId;

    /**
     * 판매자 ID (FK 대신 Long 사용)
     */
    @Column(nullable = false)
    private Long sellerId;

    /**
     * 결제 수단
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    /**
     * 결제 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    /**
     * 결제 실패 메시지
     */
    @Column(length = 500)
    private String paymentFailMsg;

    /**
     * 결제 취소
     */
    public void cancel(String cancelReason) {
        this.status = PaymentStatus.CANCEL;
        this.cancelReason = cancelReason;
        this.canceledAt = LocalDateTime.now();
    }
}
