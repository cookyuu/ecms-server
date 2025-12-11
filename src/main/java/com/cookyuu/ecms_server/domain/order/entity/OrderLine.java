package com.cookyuu.ecms_server.domain.order.entity;

import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false, length = 200)
    private String productName;

    @Column(length = 1000)
    private String productDescription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_line_order"))
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_line_product"))
    private Product product;

    public void setOrder(Order order) {
        this.order = order;
        if (order != null && !order.getOrderLines().contains(this)) {
            order.getOrderLines().add(this);
        }
    }

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

    public Integer getTotalPrice() {
        return price * quantity;
    }
}
