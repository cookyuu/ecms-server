package com.cookyuu.ecms_server.domain.cart.repository;

import com.cookyuu.ecms_server.domain.cart.entity.Cart;
import com.cookyuu.ecms_server.domain.cart.entity.CartItem;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    boolean existsByCartAndProduct(Cart cart, Product product);

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    @Query("SELECT ci FROM CartItem ci " +
           "LEFT JOIN FETCH ci.product " +
           "WHERE ci.cart = :cart AND ci.product = :product")
    Optional<CartItem> findByCartAndProductWithProduct(@Param("cart") Cart cart, @Param("product") Product product);
}
