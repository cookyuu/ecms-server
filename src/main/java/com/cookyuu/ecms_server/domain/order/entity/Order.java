package com.cookyuu.ecms_server.domain.order.entity;

import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.global.entity.BaseTimeEntity;
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
    private LocalDateTime canceledAt;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private Member buyer;

    @OneToMany(mappedBy = "order")
    private List<OrderLine> orderLines = new ArrayList<>();

    @Builder
    public Order (Integer totalPrice, String orderNumber, OrderStatus status, Member buyer, List<OrderLine> orderLines) {
        this.totalPrice = totalPrice;
        this.orderNumber = orderNumber;
        this.status = status;
        this.buyer = buyer;
        this.orderLines = orderLines;
    }

    public void cancelReq(String cancelReason) {
        this.cancelReason = cancelReason;
        this.status = OrderStatus.CANCEL_WAIT;
    }

    public void reviseOrder(int totalPrice) {
        this.status = OrderStatus.ORDER_COMPLETE;
        this.totalPrice = totalPrice;
    }
}
