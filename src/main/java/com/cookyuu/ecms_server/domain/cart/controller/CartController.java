package com.cookyuu.ecms_server.domain.cart.controller;

import com.cookyuu.ecms_server.domain.cart.dto.DeleteCartItemDto;
import com.cookyuu.ecms_server.domain.cart.dto.UpdateCartItemDto;
import com.cookyuu.ecms_server.domain.cart.service.CartService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    @PutMapping("/item")
    public ResponseEntity<ApiResponse<Object>> updateCartItem(@AuthenticationPrincipal UserDetails user, @RequestBody UpdateCartItemDto.Request cartItemInfo) {
        cartService.updateCartItem(user, cartItemInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/item")
    public ResponseEntity<ApiResponse<Object>> deleteCartItem(@AuthenticationPrincipal UserDetails user, @RequestBody DeleteCartItemDto.Request cartItemInfo) {
        cartService.deleteCartItem(user, cartItemInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
