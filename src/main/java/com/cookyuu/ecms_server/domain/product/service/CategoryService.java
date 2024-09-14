package com.cookyuu.ecms_server.domain.product.service;

import com.cookyuu.ecms_server.domain.product.dto.RegisterCategoryDto;
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

    public Category findByName(String name) {
        Category category = categoryRepository.findByName(name).orElseThrow(ECMSCategoryException::new);
        log.info("[FindCategoryByName] Find category OK!, category Id : {}", category.getId());
        return category;
    }

    @Transactional
    public Long registerCategory(RegisterCategoryDto.Request categoryInfo) {
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

    private void chkCategoryNameDuplicated(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new ECMSCategoryException(ResultCode.CATEGORY_NAME_DUPLICATED);
        }
        log.info("[CheckCategoryNameDuplication] Duplication check OK!, CategoryName : {}", name);
    }
}
