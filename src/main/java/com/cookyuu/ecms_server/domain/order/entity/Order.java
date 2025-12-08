package com.cookyuu.ecms_server.domain.order.entity;

import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.order.enums.OrderStatus;
import com.cookyuu.ecms_server.domain.shipment.entity.Shipment;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.domain.BaseTimeEntity;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "ecms_order",
        indexes = {
                @Index(name = "ecms_order_search_idx_1", columnList = "status"),
                @Index(name = "ecms_order_search_idx_2", columnList = "orderNumber", unique = true),
                @Index(name = "ecms_order_search_idx_3", columnList = "buyer_id")
        }
)
public class Order extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer totalPrice;

    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(length = 500)
    private String cancelReason;

    @Column(nullable = false)
    private boolean isCanceled = false;

    private LocalDateTime canceledAt;

    @Column(nullable = false, length = 200)
    private String destination;

    @Column(length = 200)
    private String destinationDetail;

    @Column(nullable = false, length = 50)
    private String recipientName;

    @Column(nullable = false, length = 20)
    private String recipientPhoneNumber;

    @Column(length = 500)
    private String paymentFailMsg;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_buyer"))
    private Member buyer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> orderLines = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", foreignKey = @ForeignKey(name = "fk_order_shipment"))
    private Shipment shipment;

    @Builder
    public Order (Integer totalPrice, String orderNumber, OrderStatus status, Member buyer, List<OrderLine> orderLines, String destination, String destinationDetail, String recipientName, String recipientPhoneNumber) {
        this.totalPrice = totalPrice;
        this.orderNumber = orderNumber;
        this.status = status;
        this.buyer = buyer;
        this.orderLines = orderLines != null ? orderLines : new ArrayList<>();
        this.destination = destination;
        this.destinationDetail = destinationDetail;
        this.recipientName = recipientName;
        this.recipientPhoneNumber = recipientPhoneNumber;
    }

    public void cancel(String cancelReason) {
        this.cancelReason = cancelReason;
        this.status = OrderStatus.CANCELED;
        this.isCanceled = true;
        this.canceledAt = LocalDateTime.now();
    }

    public void reviseOrder(int totalPrice) {
        this.status = OrderStatus.ORDER_COMPLETE;
        this.totalPrice = totalPrice;
    }

    public void validateNotCanceled() {
        if (isCanceled) {
            throw new BusinessException(ResultCode.ALREADY_CANCELED_ORDER);
        }
    }

    public void validatePaymentComplete() {
        if (!this.status.equals(OrderStatus.PAYMENT_COMPLETE)) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
    }

    public void successPayment() {
        this.status = OrderStatus.PAYMENT_COMPLETE;
    }

    public void successShipment(Shipment shipment) {
        this.status = OrderStatus.SHIPPING;
        this.shipment = shipment;
    }

    public void failPayment(String paymentFailMsg) {
        this.status = OrderStatus.PAYMENT_FAIL;
        this.paymentFailMsg = paymentFailMsg;
    }

    public void cancelPayment() {
        this.status = OrderStatus.PAYMENT_CANCEL;
    }
}
