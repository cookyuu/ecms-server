package com.cookyuu.ecms_server.domain.product.service;

import com.cookyuu.ecms_server.domain.product.dto.CategoryInfoDto;
import com.cookyuu.ecms_server.domain.product.entity.Category;
import com.cookyuu.ecms_server.domain.product.repository.CategoryRepository;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    public Long registerCategory(CategoryInfoDto.Request categoryInfo) {
        Category parentCategory = null;
        if (!StringUtils.isEmpty(categoryInfo.getParentCategoryName())) {
            parentCategory = findByName(categoryInfo.getParentCategoryName());
        }
        chkCategoryNameDuplicated(categoryInfo.getName());

        Category registerCategory = Category.of(categoryInfo.getName(), parentCategory);
        Category category = categoryRepository.save(registerCategory);
        log.atInfo()
                .addKeyValue(EVENT, CATEGORY_REGISTERED)
                .addKeyValue(CATEGORY_ID, category.getId())
                .addKeyValue(CATEGORY_NAME, category.getName())
                .addKeyValue(PARENT_CATEGORY_NAME, categoryInfo.getParentCategoryName())
                .log("Category registered successfully");
        return category.getId();
    }

    @Transactional
    public void updateCategory(Long categoryId, CategoryInfoDto.Request categoryInfo) {
        Category category = findById(categoryId);
        Category parentCategory = null;
        if (!StringUtils.isEmpty(categoryInfo.getParentCategoryName())) {
            parentCategory = findByName(categoryInfo.getParentCategoryName());
        }
        category.update(categoryInfo.getName(), parentCategory);
        log.atInfo()
                .addKeyValue(EVENT, CATEGORY_UPDATED)
                .addKeyValue(CATEGORY_ID, categoryId)
                .addKeyValue(CATEGORY_NAME, categoryInfo.getName())
                .log("Category updated successfully");
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = findById(categoryId);
        categoryRepository.delete(category);
        log.atInfo()
                .addKeyValue(EVENT, CATEGORY_DELETED)
                .addKeyValue(CATEGORY_ID, categoryId)
                .addKeyValue(CATEGORY_NAME, category.getName())
                .log("Category deleted successfully");
    }

    public Category findByName(String name) {
        Category category = categoryRepository.findByName(name).orElseThrow(() -> new BusinessException(ResultCode.CATEGORY_NOT_FOUND));
        log.atDebug()
                .addKeyValue(CATEGORY_ID, category.getId())
                .addKeyValue(CATEGORY_NAME, name)
                .log("Category found by name");
        return category;
    }

    public Category findById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new BusinessException(ResultCode.CATEGORY_NOT_FOUND));
        log.atDebug()
                .addKeyValue(CATEGORY_ID, categoryId)
                .addKeyValue(CATEGORY_NAME, category.getName())
                .log("Category found by ID");
        return category;
    }
    private void chkCategoryNameDuplicated(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new BusinessException(ResultCode.CATEGORY_NAME_DUPLICATED);
        }
        log.atDebug()
                .addKeyValue(CATEGORY_NAME, name)
                .log("Category name duplication check passed");
    }
}
