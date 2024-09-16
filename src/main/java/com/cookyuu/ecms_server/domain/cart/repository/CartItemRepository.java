package com.cookyuu.ecms_server.domain.cart.repository;

import com.cookyuu.ecms_server.domain.cart.entity.Cart;
import com.cookyuu.ecms_server.domain.cart.entity.CartItem;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    boolean existsByCartAndProduct(Cart cart, Product product);

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
