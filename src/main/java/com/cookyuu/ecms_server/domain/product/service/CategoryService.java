package com.cookyuu.ecms_server.domain.product.service;

import com.cookyuu.ecms_server.domain.product.dto.CategoryInfoDto;
import com.cookyuu.ecms_server.domain.product.entity.Category;
import com.cookyuu.ecms_server.domain.product.repository.CategoryRepository;
import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.domain.ECMSCategoryException;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        log.info("[RegisterCategory] Category Registration OK!!");
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
        log.info("[UpdateCategory] Update category OK!");
    }

    public Category findByName(String name) {
        Category category = categoryRepository.findByName(name).orElseThrow(ECMSCategoryException::new);
        log.info("[FindCategoryByName] Find category OK!, category Id : {}", category.getId());
        return category;
    }

    public Category findById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(ECMSCategoryException::new);
        log.info("[FindCategoryByName] Find category OK!, category Name : {}", category.getName());
        return category;
    }

    private void chkCategoryNameDuplicated(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new ECMSCategoryException(ResultCode.CATEGORY_NAME_DUPLICATED);
        }
        log.info("[CheckCategoryNameDuplication] Duplication check OK!, CategoryName : {}", name);
    }
}
