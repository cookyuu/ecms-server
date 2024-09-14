package com.cookyuu.ecms_server.domain.product.controller;

import com.cookyuu.ecms_server.domain.product.dto.CategoryInfoDto;
import com.cookyuu.ecms_server.domain.product.service.CategoryService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/category")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("registration")
    public ResponseEntity<ApiResponse<CategoryInfoDto.Response>> registerCategory(@RequestBody CategoryInfoDto.Request categoryInfo) {
        Long categoryId = categoryService.registerCategory(categoryInfo);
        return ResponseEntity.ok(ApiResponse.created(
                CategoryInfoDto.Response.builder()
                        .categoryId(categoryId)
                        .build()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> updateCategory(@PathVariable("id") Long categoryId, @RequestBody CategoryInfoDto.Request categoryInfo) {
        categoryService.updateCategory(categoryId, categoryInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
