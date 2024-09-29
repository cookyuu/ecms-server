package com.cookyuu.ecms_server.domain.order.entity;

import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import com.cookyuu.ecms_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private Member member;

    @OneToMany(mappedBy = "order")
    private List<OrderLine> orderLines = new ArrayList<>();

    @Builder
    public Order (Integer totalPrice, String orderNumber, OrderStatus status, Member member, List<OrderLine> orderLines) {
        this.totalPrice = totalPrice;
        this.orderNumber = orderNumber;
        this.status = status;
        this.member = member;
        this.orderLines = orderLines;
    }
}
