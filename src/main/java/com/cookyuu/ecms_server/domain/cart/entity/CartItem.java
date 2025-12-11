package com.cookyuu.ecms_server.domain.cart.entity;

import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

/**
 * 장바구니 아이템 엔티티
 *
 * - 장바구니(Cart) N:1 관계
 * - 상품(Product) N:1 관계
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "ecms_cart_item",
        indexes = {
                @Index(name = "ecms_cart_item_idx_1", columnList = "cart_id"),
                @Index(name = "ecms_cart_item_idx_2", columnList = "product_id")
        }
)
public class CartItem extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 수량
     */
    @NotNull
    @Column(nullable = false)
    @ColumnDefault("1")
    @Builder.Default
    private Integer quantity = 1;

    /**
     * 장바구니 (필수)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_item_cart"))
    private Cart cart;

    /**
     * 상품 (필수)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_item_product"))
    private Product product;

    /**
     * 수량 변경
     */
    public void updateQuantity(Integer quantity) {
        if (quantity != null && quantity > 0) {
            this.quantity = quantity;
        }
    }

    /**
     * 양방향 연관관계 편의 메서드
     */
    void setCart(Cart cart) {
        this.cart = cart;
    }
}
