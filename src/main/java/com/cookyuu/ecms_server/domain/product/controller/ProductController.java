package com.cookyuu.ecms_server.domain.product.controller;

import com.cookyuu.ecms_server.domain.product.dto.RegisterProductDto;
import com.cookyuu.ecms_server.domain.product.dto.UpdateProductDto;
import com.cookyuu.ecms_server.domain.product.service.ProductService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import com.cookyuu.ecms_server.global.dto.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product")
@PreAuthorize("hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
public class ProductController {
    private final ProductService productService;
    private Long id;

    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<Object>> registerProduct(@AuthenticationPrincipal UserDetails user, @RequestBody RegisterProductDto.Request productInfo) {
        productService.registerProduct(user, productInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> updateProduct(@PathVariable("id") Long productId, @AuthenticationPrincipal UserDetails user, @RequestBody UpdateProductDto.Request productInfo) {
        productService.updateProduct(productId, user, productInfo);
        return ResponseEntity.ok(ApiResponse.success(ResultCode.PRODUCT_UPDATE_SUCCESS));
    }

}
