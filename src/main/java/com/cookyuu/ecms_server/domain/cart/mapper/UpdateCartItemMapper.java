package com.cookyuu.ecms_server.domain.cart.mapper;

import com.cookyuu.ecms_server.domain.cart.dto.UpdateCartItemDto;
import com.cookyuu.ecms_server.domain.cart.entity.Cart;
import com.cookyuu.ecms_server.domain.cart.entity.CartItem;
import com.cookyuu.ecms_server.domain.product.entity.Product;

public class UpdateCartItemMapper {
    public static CartItem toEntity(UpdateCartItemDto.Request cartItemInfo, Product product, Cart cart) {
        return CartItem.builder()
                .quantity(cartItemInfo.getQuantity())
                .product(product)
                .cart(cart)
                .build();
    }
}
