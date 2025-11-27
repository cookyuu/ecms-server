package com.cookyuu.ecms_server.domain.cart.repository;

import com.cookyuu.ecms_server.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByMemberId(Long memberId);

    @Query("SELECT c FROM Cart c " +
           "LEFT JOIN FETCH c.cartItems ci " +
           "LEFT JOIN FETCH ci.product " +
           "WHERE c.member.id = :memberId")
    Optional<Cart> findByMemberIdWithCartItemsAndProducts(@Param("memberId") Long memberId);
}
