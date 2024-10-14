package com.cookyuu.ecms_server.domain.order.entity;

import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.shipment.entity.Shipment;
import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.entity.BaseTimeEntity;
import com.cookyuu.ecms_server.global.exception.domain.ECMSOrderException;
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
@Table(name = "ecms_order")
public class Order extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer totalPrice;
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String cancelReason;
    private boolean isCanceled;
    private LocalDateTime canceledAt;

    private String destination;
    private String destinationDetail;
    private String recipientName;
    private String recipientPhoneNumber;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private Member buyer;

    @OneToMany(mappedBy = "order")
    private List<OrderLine> orderLines = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    @Builder
    public Order (Integer totalPrice, String orderNumber, OrderStatus status, Member buyer, List<OrderLine> orderLines, String destination, String destinationDetail, String recipientName, String recipientPhoneNumber) {
        this.totalPrice = totalPrice;
        this.orderNumber = orderNumber;
        this.status = status;
        this.buyer = buyer;
        this.orderLines = orderLines;
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

    public void isCanceled() {
        if (isCanceled) {
            throw new ECMSOrderException(ResultCode.ALREADY_CANCELED_ORDER);
        }
    }

    public void isPaymentComplete() {
        if (!this.status.equals(OrderStatus.PAYMENT_COMPLETE)) {
            throw new ECMSOrderException(ResultCode.ORDER_STATUS_ERROR);
        }
    }

    public void successPayment() {
        this.status = OrderStatus.PAYMENT_COMPLETE;
    }

    public void successShipment(Shipment shipment) {
        this.status = OrderStatus.SHIPPING;
        this.shipment = shipment;
    }

    public void failPayment() {
        this.status = OrderStatus.PAYMENT_FAIL;
    }

    public void cancelPayment() {
        this.status = OrderStatus.PAYMENT_CANCEL;
    }
}
