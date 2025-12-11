package com.cookyuu.ecms_server.domain.cart.controller;

import com.cookyuu.ecms_server.domain.cart.dto.DeleteCartItemDto;
import com.cookyuu.ecms_server.domain.cart.dto.UpdateCartItemDto;
import com.cookyuu.ecms_server.domain.cart.service.CartService;
import com.cookyuu.ecms_server.common.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "장바구니 API", description = "장바구니 상품 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "장바구니 상품 수정", description = "장바구니에 담긴 상품의 수량을 수정합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "장바구니 상품을 찾을 수 없음")
    })
    @PutMapping("/item")
    public ResponseEntity<ApiResponse<Object>> updateCartItem(@AuthenticationPrincipal UserDetails user, @RequestBody UpdateCartItemDto.Request cartItemInfo) {
        cartService.updateCartItem(user, cartItemInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "장바구니 상품 삭제", description = "장바구니에서 상품을 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "장바구니 상품을 찾을 수 없음")
    })
    @DeleteMapping("/item")
    public ResponseEntity<ApiResponse<Object>> deleteCartItem(@AuthenticationPrincipal UserDetails user, @RequestBody DeleteCartItemDto.Request cartItemInfo) {
        cartService.deleteCartItem(user, cartItemInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
