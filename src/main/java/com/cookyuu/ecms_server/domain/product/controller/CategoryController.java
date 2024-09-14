package com.cookyuu.ecms_server.domain.product.controller;

import com.cookyuu.ecms_server.domain.product.dto.RegisterCategoryDto;
import com.cookyuu.ecms_server.domain.product.service.CategoryService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/category")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("registration")
    public ResponseEntity<ApiResponse<RegisterCategoryDto.Response>> registerCategory(@RequestBody RegisterCategoryDto.Request categoryInfo) {
        Long categoryId = categoryService.registerCategory(categoryInfo);
        return ResponseEntity.ok(ApiResponse.created(
                RegisterCategoryDto.Response.builder()
                        .categoryId(categoryId)
                        .build()
        ));
    }
}
