package com.cookyuu.ecms_server.domain.product.service;

import com.cookyuu.ecms_server.domain.product.entity.Category;
import com.cookyuu.ecms_server.domain.product.repository.CategoryRepository;
import com.cookyuu.ecms_server.global.exception.domain.ECMSCategoryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Category findByName(String name) {
        return categoryRepository.findByName(name).orElseThrow(ECMSCategoryException::new);
    }
}
