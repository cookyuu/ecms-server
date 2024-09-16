package com.cookyuu.ecms_server.domain.cart.controller;

import com.cookyuu.ecms_server.domain.cart.dto.AddCartItemDto;
import com.cookyuu.ecms_server.domain.cart.service.CartService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping("/item")
    public ResponseEntity<ApiResponse<Object>> addCartItem(@AuthenticationPrincipal UserDetails user, @RequestBody AddCartItemDto.Request cartItemInfo) {
        cartService.addCartItem(user, cartItemInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
