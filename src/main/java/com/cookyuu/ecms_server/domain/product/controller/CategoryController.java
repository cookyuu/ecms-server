package com.cookyuu.ecms_server.domain.product.controller;

import com.cookyuu.ecms_server.domain.product.dto.CategoryInfoDto;
import com.cookyuu.ecms_server.domain.product.service.CategoryService;
import com.cookyuu.ecms_server.common.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "카테고리 API", description = "상품 카테고리 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/category")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 등록", description = "새로운 상품 카테고리를 등록합니다. 관리자 권한이 필요합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 카테고리")
    })
    @PostMapping("registration")
    public ResponseEntity<ApiResponse<CategoryInfoDto.Response>> registerCategory(@RequestBody CategoryInfoDto.Request categoryInfo) {
        Long categoryId = categoryService.registerCategory(categoryInfo);
        return ResponseEntity.ok(ApiResponse.created(
                CategoryInfoDto.Response.builder()
                        .categoryId(categoryId)
                        .build()
        ));
    }

    @Operation(summary = "카테고리 수정", description = "기존 카테고리의 정보를 수정합니다. 관리자 권한이 필요합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> updateCategory(
        @Parameter(description = "수정할 카테고리 ID", required = true)
        @PathVariable("id") Long categoryId,
        @RequestBody CategoryInfoDto.Request categoryInfo) {
        categoryService.updateCategory(categoryId, categoryInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제합니다. 관리자 권한이 필요합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    @DeleteMapping()
    public ResponseEntity<ApiResponse<Object>> deleteCategory(
        @Parameter(description = "삭제할 카테고리 ID", required = true)
        @RequestParam("id") Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
