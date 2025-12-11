package com.cookyuu.ecms_server.domain.product.controller;

import com.cookyuu.ecms_server.domain.product.dto.FindProductDetailDto;
import com.cookyuu.ecms_server.domain.product.dto.RegisterProductDto;
import com.cookyuu.ecms_server.domain.product.dto.SearchProductDto;
import com.cookyuu.ecms_server.domain.product.dto.UpdateProductDto;
import com.cookyuu.ecms_server.domain.product.service.ProductService;
import com.cookyuu.ecms_server.common.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "상품 API", description = "상품 등록, 조회, 수정, 삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product")
public class ProductController {
    private final ProductService productService;
    private Long id;

    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다. 판매자 권한이 필요합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "판매자 권한 없음")
    })
    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<RegisterProductDto.Response>> registerProduct(@AuthenticationPrincipal UserDetails user, @RequestBody RegisterProductDto.Request productInfo) {
        Long productId = productService.registerProduct(user, productInfo);
        return ResponseEntity.ok(
                ApiResponse.success(RegisterProductDto.Response.builder().productId(productId).build()));
    }

    @Operation(summary = "상품 정보 수정", description = "기존 상품의 정보를 수정합니다. 해당 상품을 등록한 판매자만 수정 가능합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> updateProduct(
        @Parameter(description = "수정할 상품 ID", required = true)
        @PathVariable("id") Long productId,
        @AuthenticationPrincipal UserDetails user,
        @RequestBody UpdateProductDto.Request productInfo) {
        productService.updateProduct(productId, user, productInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다. 해당 상품을 등록한 판매자만 삭제 가능합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    @DeleteMapping()
    public ResponseEntity<ApiResponse<Object>> deleteProduct(
        @Parameter(description = "삭제할 상품 ID", required = true)
        @RequestParam("id") Long productId,
        @AuthenticationPrincipal UserDetails user) {
        productService.deleteProduct(productId, user);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "상품 상세 조회", description = "상품의 상세 정보를 조회합니다. 조회수가 자동으로 증가합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<FindProductDetailDto>> findProductDetail(
        @Parameter(description = "조회할 상품 ID", required = true)
        @PathVariable(name = "productId") Long productId,
        HttpServletRequest request,
        HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.success(productService.findProductDetail(productId, request, response)));
    }

    @Operation(summary = "상품 검색", description = "키워드와 옵션으로 상품을 검색합니다. 페이징을 지원합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색 성공")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SearchProductDto.Response>>> searchProductList(
        @Parameter(description = "검색 옵션 (NAME, CATEGORY 등)")
        @RequestParam(name = "option", required = false) String option,
        @Parameter(description = "검색 키워드")
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
