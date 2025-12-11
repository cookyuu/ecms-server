package com.cookyuu.ecms_server.domain.cart.entity;

import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 장바구니 엔티티
 *
 * - 회원(Member) N:1 관계
 * - 장바구니 아이템(CartItem) 1:N 관계 (cascade)
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "ecms_cart",
        indexes = {
                @Index(name = "ecms_cart_search_idx_1", columnList = "member_id")
        }
)
public class Cart extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 회원 (필수)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_member"))
    private Member member;

    /**
     * 장바구니 아이템 목록
     * - 장바구니 삭제 시 아이템도 함께 삭제 (cascade)
     * - 고아 객체 자동 제거 (orphanRemoval)
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();

    /**
     * 양방향 연관관계 편의 메서드
     */
    public void addCartItem(CartItem cartItem) {
        cartItems.add(cartItem);
        if (cartItem.getCart() != this) {
            cartItem.setCart(this);
        }
    }

    public void removeCartItem(CartItem cartItem) {
        cartItems.remove(cartItem);
    }
}
