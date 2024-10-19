package com.cookyuu.ecms_server.domain.order.dto;

import com.cookyuu.ecms_server.domain.product.entity.Product;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderItemInfo {
    @NotNull
    private Long productId;
    @NotNull
    private Integer price;
    @NotNull
    private Integer quantity;
    private Product product;

    @Builder
    public CreateOrderItemInfo(Long productId, Integer price, Integer quantity) {
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
    }

    public void addProduct(Product product) {
        this.product = product;
    }
}
