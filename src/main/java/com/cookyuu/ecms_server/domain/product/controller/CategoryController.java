package com.cookyuu.ecms_server.domain.product.controller;

import com.cookyuu.ecms_server.domain.product.dto.CategoryInfoDto;
import com.cookyuu.ecms_server.domain.product.service.CategoryService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/category")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class CategoryController {

    private final CategoryService categoryService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("registration")
    public ResponseEntity<ApiResponse<CategoryInfoDto.Response>> registerCategory(@RequestBody CategoryInfoDto.Request categoryInfo) {
        Long categoryId = categoryService.registerCategory(categoryInfo);
        return ResponseEntity.ok(ApiResponse.created(
                CategoryInfoDto.Response.builder()
                        .categoryId(categoryId)
                        .build()
        ));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> updateCategory(@PathVariable("id") Long categoryId, @RequestBody CategoryInfoDto.Request categoryInfo) {
        categoryService.updateCategory(categoryId, categoryInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping()
    public ResponseEntity<ApiResponse<Object>> deleteCategory(@RequestParam("id") Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
