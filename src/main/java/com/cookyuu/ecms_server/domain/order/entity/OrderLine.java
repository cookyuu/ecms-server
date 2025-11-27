package com.cookyuu.ecms_server.domain.order.entity;

import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 상품 라인 엔티티
 *
 * 주문 시점의 상품 정보를 스냅샷으로 저장 (비정규화)
 * - 상품이 삭제되거나 가격이 변경되어도 주문 내역 유지
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ecms_order_line")
public class OrderLine extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 주문 수량
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * 주문 시점의 개당 가격 (스냅샷)
     */
    @Column(nullable = false)
    private Integer price;

    /**
     * 주문 시점의 상품명 (스냅샷)
     * 상품 삭제 후에도 주문 내역에서 확인 가능
     */
    @Column(nullable = false, length = 200)
    private String productName;

    /**
     * 주문 시점의 상품 설명 (스냅샷)
     */
    @Column(length = 1000)
    private String productDescription;

    /**
     * 주문 참조 (필수)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_line_order"))
    private Order order;

    /**
     * 상품 참조 (필수)
     * Soft Delete 방식이므로 삭제 후에도 참조 유지
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_line_product"))
    private Product product;

    /**
     * 양방향 연관관계 편의 메서드
     */
    public void setOrder(Order order) {
        this.order = order;
        if (order != null && !order.getOrderLines().contains(this)) {
            order.getOrderLines().add(this);
        }
    }

    /**
     * 상품 정보 스냅샷 생성
     */
    public static OrderLine createSnapshot(Order order, Product product, Integer quantity) {
        OrderLine orderLine = OrderLine.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .price(product.getPrice())
                .productName(product.getName())
                .productDescription(product.getDescription())
                .build();

        orderLine.setOrder(order);
        return orderLine;
    }

    /**
     * 총 가격 계산
     */
    public Integer getTotalPrice() {
        return price * quantity;
    }
}
