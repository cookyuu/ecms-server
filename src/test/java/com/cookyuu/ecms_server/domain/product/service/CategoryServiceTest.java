package com.cookyuu.ecms_server.domain.product.service;

import com.cookyuu.ecms_server.domain.product.dto.CategoryInfoDto;
import com.cookyuu.ecms_server.domain.product.entity.Category;
import com.cookyuu.ecms_server.domain.product.repository.CategoryRepository;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @DisplayName("부모 카테고리가 있는 카테고리 정보가 주어질 때 카테고리를 등록하면 성공한다")
    @Test
    void givenCategoryInfoWithParent_whenRegisterCategory_thenSuccess() {
        // Given
        CategoryInfoDto.Request request = new CategoryInfoDto.Request("전자기기", "가전제품");
        Category parentCategory = createCategory(1L, "가전제품", null);
        Category savedCategory = createCategory(2L, "전자기기", parentCategory);

        when(categoryRepository.findByName("가전제품")).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.existsByName("전자기기")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // When
        Long categoryId = categoryService.registerCategory(request);

        // Then
        assertThat(categoryId).isEqualTo(2L);

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(1)).save(categoryCaptor.capture());
        Category capturedCategory = categoryCaptor.getValue();
        assertThat(capturedCategory.getName()).isEqualTo("전자기기");
        assertThat(capturedCategory.getParent()).isEqualTo(parentCategory);
    }

    @DisplayName("부모 카테고리가 없는 카테고리 정보가 주어질 때 카테고리를 등록하면 성공한다")
    @Test
    void givenCategoryInfoWithoutParent_whenRegisterCategory_thenSuccess() {
        // Given
        CategoryInfoDto.Request request = new CategoryInfoDto.Request("의류", "");
        Category savedCategory = createCategory(1L, "의류", null);

        when(categoryRepository.existsByName("의류")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // When
        Long categoryId = categoryService.registerCategory(request);

        // Then
        assertThat(categoryId).isEqualTo(1L);

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(1)).save(categoryCaptor.capture());
        Category capturedCategory = categoryCaptor.getValue();
        assertThat(capturedCategory.getName()).isEqualTo("의류");
        assertThat(capturedCategory.getParent()).isNull();
    }

    @DisplayName("중복된 카테고리 이름이 주어질 때 카테고리를 등록하면 예외가 발생한다")
    @Test
    void givenDuplicateCategoryName_whenRegisterCategory_thenThrowException() {
        // Given
        CategoryInfoDto.Request request = new CategoryInfoDto.Request("전자기기", "");

        when(categoryRepository.existsByName("전자기기")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.registerCategory(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.CATEGORY_NAME_DUPLICATED);

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @DisplayName("존재하지 않는 부모 카테고리가 주어질 때 카테고리를 등록하면 예외가 발생한다")
    @Test
    void givenNonExistentParentCategory_whenRegisterCategory_thenThrowException() {
        // Given
        CategoryInfoDto.Request request = new CategoryInfoDto.Request("전자기기", "가전제품");

        when(categoryRepository.findByName("가전제품")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.registerCategory(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.CATEGORY_NOT_FOUND);

        verify(categoryRepository, never()).existsByName(anyString());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @DisplayName("새로운 부모 카테고리가 주어질 때 카테고리를 수정하면 성공한다")
    @Test
    void givenNewParentCategory_whenUpdateCategory_thenSuccess() {
        // Given
        Long categoryId = 1L;
        CategoryInfoDto.Request request = new CategoryInfoDto.Request("스마트폰", "전자기기");
        Category category = createCategory(categoryId, "휴대폰", null);
        Category newParent = createCategory(2L, "전자기기", null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("전자기기")).thenReturn(Optional.of(newParent));

        // When
        categoryService.updateCategory(categoryId, request);

        // Then
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).findByName("전자기기");
    }

    @DisplayName("부모 카테고리 없이 수정할 때 카테고리 이름만 변경되면 성공한다")
    @Test
    void givenNoParentCategory_whenUpdateCategory_thenUpdateNameOnly() {
        // Given
        Long categoryId = 1L;
        CategoryInfoDto.Request request = new CategoryInfoDto.Request("의류", "");
        Category category = createCategory(categoryId, "옷", null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // When
        categoryService.updateCategory(categoryId, request);

        // Then
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, never()).findByName(anyString());
    }

    @DisplayName("존재하지 않는 카테고리 ID가 주어질 때 수정하면 예외가 발생한다")
    @Test
    void givenNonExistentCategoryId_whenUpdateCategory_thenThrowException() {
        // Given
        Long categoryId = 999L;
        CategoryInfoDto.Request request = new CategoryInfoDto.Request("전자기기", "");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.CATEGORY_NOT_FOUND);
    }

    @DisplayName("유효한 카테고리 ID가 주어질 때 카테고리를 삭제하면 성공한다")
    @Test
    void givenValidCategoryId_whenDeleteCategory_thenSuccess() {
        // Given
        Long categoryId = 1L;
        Category category = createCategory(categoryId, "전자기기", null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).delete(category);

        // When
        categoryService.deleteCategory(categoryId);

        // Then
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).delete(category);
    }

    @DisplayName("존재하지 않는 카테고리 ID가 주어질 때 삭제하면 예외가 발생한다")
    @Test
    void givenNonExistentCategoryId_whenDeleteCategory_thenThrowException() {
        // Given
        Long categoryId = 999L;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.CATEGORY_NOT_FOUND);

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @DisplayName("유효한 카테고리 이름이 주어질 때 이름으로 조회하면 성공한다")
    @Test
    void givenValidCategoryName_whenFindByName_thenSuccess() {
        // Given
        String categoryName = "전자기기";
        Category category = createCategory(1L, categoryName, null);

        when(categoryRepository.findByName(categoryName)).thenReturn(Optional.of(category));

        // When
        Category result = categoryService.findByName(categoryName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(categoryName);
        verify(categoryRepository, times(1)).findByName(categoryName);
    }

    @DisplayName("존재하지 않는 카테고리 이름이 주어질 때 이름으로 조회하면 예외가 발생한다")
    @Test
    void givenNonExistentCategoryName_whenFindByName_thenThrowException() {
        // Given
        String categoryName = "존재하지않는카테고리";

        when(categoryRepository.findByName(categoryName)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.findByName(categoryName))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.CATEGORY_NOT_FOUND);
    }

    @DisplayName("유효한 카테고리 ID가 주어질 때 ID로 조회하면 성공한다")
    @Test
    void givenValidCategoryId_whenFindById_thenSuccess() {
        // Given
        Long categoryId = 1L;
        Category category = createCategory(categoryId, "전자기기", null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // When
        Category result = categoryService.findById(categoryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @DisplayName("존재하지 않는 카테고리 ID가 주어질 때 ID로 조회하면 예외가 발생한다")
    @Test
    void givenNonExistentCategoryId_whenFindById_thenThrowException() {
        // Given
        Long categoryId = 999L;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.findById(categoryId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.CATEGORY_NOT_FOUND);
    }

    // Helper method
    private Category createCategory(Long id, String name, Category parent) {
        Category category = Category.builder()
                .name(name)
                .parent(parent)
                .build();
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }
}
