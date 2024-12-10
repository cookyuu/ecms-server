package com.cookyuu.ecms_server.domain.product.controller;

import com.cookyuu.ecms_server.domain.product.dto.FindProductDetailDto;
import com.cookyuu.ecms_server.domain.product.dto.RegisterProductDto;
import com.cookyuu.ecms_server.domain.product.dto.SearchProductDto;
import com.cookyuu.ecms_server.domain.product.dto.UpdateProductDto;
import com.cookyuu.ecms_server.domain.product.service.ProductService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product")
@PreAuthorize("hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
public class ProductController {
    private final ProductService productService;
    private Long id;

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<RegisterProductDto.Response>> registerProduct(@AuthenticationPrincipal UserDetails user, @RequestBody RegisterProductDto.Request productInfo) {
        Long productId = productService.registerProduct(user, productInfo);
        return ResponseEntity.ok(
                ApiResponse.success(RegisterProductDto.Response.builder().productId(productId).build()));
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> updateProduct(@PathVariable("id") Long productId, @AuthenticationPrincipal UserDetails user, @RequestBody UpdateProductDto.Request productInfo) {
        productService.updateProduct(productId, user, productInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @DeleteMapping()
    public ResponseEntity<ApiResponse<Object>> deleteProduct(@RequestParam("id") Long productId, @AuthenticationPrincipal UserDetails user) {
        productService.deleteProduct(productId, user);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<FindProductDetailDto>> findProductDetail(@PathVariable(name = "productId") Long productId, HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.success(productService.findProductDetail(productId, request, response)));
    }
    @PreAuthorize("permitAll()")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SearchProductDto.Response>>> searchProductList(@RequestParam(name = "option", required = false) String option,
                                                                                      @RequestParam(name = "keyword", required = false) String keyword,
                                                                                      Pageable pageable) {
        SearchProductDto.Request req = SearchProductDto.Request.builder()
                .option(option)
                .keyword(keyword)
                .pageable(pageable)
                .build();
        Page<SearchProductDto.Response> resProductList = productService.searchProductList(req);
        return ResponseEntity.ok(ApiResponse.success(resProductList));
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SearchProductDto.Response>>> searchProductList(@RequestParam(name = "option", required = false) String option,
                                                                                          @RequestParam(name = "keyword", required = false) String keyword,
                                                                                          Pageable pageable) {
        SearchProductDto.Request req = SearchProductDto.Request.builder()
                .option(option)
                .keyword(keyword)
                .pageable(pageable)
                .build();
        Page<SearchProductDto.Response> resProductList = productService.searchProductList(req);
        return ResponseEntity.ok(ApiResponse.success(resProductList));
    }
}
