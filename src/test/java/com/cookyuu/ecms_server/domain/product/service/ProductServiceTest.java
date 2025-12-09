package com.cookyuu.ecms_server.domain.product.service;

import com.cookyuu.ecms_server.common.enums.CookieCode;
import com.cookyuu.ecms_server.common.enums.RedisKeyCode;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.utils.CookieUtils;
import com.cookyuu.ecms_server.common.utils.RedisUtils;
import com.cookyuu.ecms_server.domain.product.dto.FindProductDetailDto;
import com.cookyuu.ecms_server.domain.product.dto.RegisterProductDto;
import com.cookyuu.ecms_server.domain.product.dto.SearchProductDto;
import com.cookyuu.ecms_server.domain.product.dto.UpdateProductDto;
import com.cookyuu.ecms_server.domain.product.entity.Category;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.domain.product.logging.ProductLogHelper;
import com.cookyuu.ecms_server.domain.product.repository.ProductRepository;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import com.cookyuu.ecms_server.domain.seller.service.SellerService;
import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private SellerService sellerService;

    @Mock
    private RedisUtils redisUtil;

    @Mock
    private CookieUtils cookieUtils;

    @Mock
    private ProductLogHelper productLogHelper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    // ==================== registerProduct 테스트 ====================

    @DisplayName("유효한 상품 정보가 주어질 때 상품 등록을 하면 성공한다")
    @Test
    void givenValidProductInfo_whenRegisterProduct_thenSuccess() {
        // Given
        UserDetails user = User.builder()
                .username("1")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        RegisterProductDto.Request productInfo = RegisterProductDto.Request.builder()
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .price(10000)
                .stockQuantity(100)
                .categoryName("전자제품")
                .build();

        Seller seller = Seller.builder()
                .name("테스트 판매자")
                .loginId("seller123")
                .password("password")
                .businessName("테스트 비즈니스")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구")
                .businessContactTelNum("02-1234-5678")
                .businessContactEmail("seller@test.com")
                .role(RoleType.SELLER)
                .build();
        ReflectionTestUtils.setField(seller, "id", 1L);

        Category category = Category.builder()
                .name("전자제품")
                .parent(null)
                .build();
        ReflectionTestUtils.setField(category, "id", 1L);

        Product savedProduct = Product.builder()
                .name(productInfo.getName())
                .description(productInfo.getDescription())
                .price(productInfo.getPrice())
                .stockQuantity(productInfo.getStockQuantity())
                .category(category)
                .seller(seller)
                .build();
        ReflectionTestUtils.setField(savedProduct, "id", 1L);

        when(sellerService.findSellerById(1L)).thenReturn(seller);
        when(categoryService.findByName(productInfo.getCategoryName())).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        Long result = productService.registerProduct(user, productInfo);

        // Then
        assertThat(result).isEqualTo(1L);

        verify(sellerService, times(1)).findSellerById(1L);
        verify(categoryService, times(1)).findByName(productInfo.getCategoryName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @DisplayName("중복된 상품 이름이 주어질 때 상품 등록을 하면 예외가 발생한다")
    @Test
    void givenDuplicateProductName_whenRegisterProduct_thenThrowException() {
        // Given
        UserDetails user = User.builder()
                .username("1")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        RegisterProductDto.Request productInfo = RegisterProductDto.Request.builder()
                .name("중복 상품")
                .description("테스트 상품 설명")
                .price(10000)
                .stockQuantity(100)
                .categoryName("전자제품")
                .build();

        Seller seller = Seller.builder()
                .name("테스트 판매자")
                .loginId("seller123")
                .password("password")
                .businessName("테스트 비즈니스")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구")
                .businessContactTelNum("02-1234-5678")
                .businessContactEmail("seller@test.com")
                .role(RoleType.SELLER)
                .build();
        ReflectionTestUtils.setField(seller, "id", 1L);

        Category category = Category.builder()
                .name("전자제품")
                .parent(null)
                .build();

        when(sellerService.findSellerById(1L)).thenReturn(seller);
        when(categoryService.findByName(productInfo.getCategoryName())).thenReturn(category);
        when(productRepository.save(any(Product.class)))
                .thenThrow(new RuntimeException("Duplicate product name"));

        // When & Then
        assertThatThrownBy(() -> productService.registerProduct(user, productInfo))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.PRODUCT_EXISTS_ALREADY);

        verify(sellerService, times(1)).findSellerById(1L);
        verify(categoryService, times(1)).findByName(productInfo.getCategoryName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    // ==================== updateProduct 테스트 ====================

    @DisplayName("유효한 상품 수정 정보가 주어질 때 상품 수정을 하면 성공한다")
    @Test
    void givenValidUpdateInfo_whenUpdateProduct_thenSuccess() {
        // Given
        Long productId = 1L;
        UserDetails user = User.builder()
                .username("1")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        UpdateProductDto.Request updateInfo = new UpdateProductDto.Request(
                "수정된 상품명",
                "수정된 설명",
                20000,
                200,
                "가전제품"
        );

        Seller seller = Seller.builder()
                .name("테스트 판매자")
                .loginId("seller123")
                .password("password")
                .businessName("테스트 비즈니스")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구")
                .businessContactTelNum("02-1234-5678")
                .businessContactEmail("seller@test.com")
                .role(RoleType.SELLER)
                .build();
        ReflectionTestUtils.setField(seller, "id", 1L);

        Category oldCategory = Category.builder()
                .name("전자제품")
                .parent(null)
                .build();

        Category newCategory = Category.builder()
                .name("가전제품")
                .parent(null)
                .build();

        Product product = Product.builder()
                .name("원본 상품명")
                .description("원본 설명")
                .price(10000)
                .stockQuantity(100)
                .category(oldCategory)
                .seller(seller)
                .build();
        ReflectionTestUtils.setField(product, "id", productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryService.findByName("가전제품")).thenReturn(newCategory);

        // When
        productService.updateProduct(productId, user, updateInfo);

        // Then
        verify(productRepository, times(1)).findById(productId);
        verify(categoryService, times(1)).findByName("가전제품");
    }

    @DisplayName("모든 필드가 null인 경우 상품 수정을 하면 예외가 발생한다")
    @Test
    void givenAllNullFields_whenUpdateProduct_thenThrowException() {
        // Given
        Long productId = 1L;
        UserDetails user = User.builder()
                .username("1")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        UpdateProductDto.Request updateInfo = new UpdateProductDto.Request(
                null, null, null, null, null
        );

        // When & Then
        assertThatThrownBy(() -> productService.updateProduct(productId, user, updateInfo))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.REQUEST_DATA_ISNULL);

        verify(productRepository, never()).findById(anyLong());
    }

    @DisplayName("상품 소유자가 아닌 경우 상품 수정을 하면 예외가 발생한다")
    @Test
    void givenNonOwner_whenUpdateProduct_thenThrowException() {
        // Given
        Long productId = 1L;
        UserDetails user = User.builder()
                .username("2")  // 다른 판매자 ID
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        UpdateProductDto.Request updateInfo = new UpdateProductDto.Request(
                "수정된 상품명", null, null, null, null
        );

        Seller seller = Seller.builder()
                .name("테스트 판매자")
                .loginId("seller123")
                .password("password")
                .businessName("테스트 비즈니스")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구")
                .businessContactTelNum("02-1234-5678")
                .businessContactEmail("seller@test.com")
                .role(RoleType.SELLER)
                .build();
        ReflectionTestUtils.setField(seller, "id", 1L);

        Category category = Category.builder()
                .name("전자제품")
                .parent(null)
                .build();

        Product product = Product.builder()
                .name("원본 상품명")
                .description("원본 설명")
                .price(10000)
                .stockQuantity(100)
                .category(category)
                .seller(seller)
                .build();
        ReflectionTestUtils.setField(product, "id", productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When & Then
        assertThatThrownBy(() -> productService.updateProduct(productId, user, updateInfo))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.PRODUCT_OWNER_UNMATCHED);

        verify(productRepository, times(1)).findById(productId);
    }

    // ==================== deleteProduct 테스트 ====================

    @DisplayName("유효한 상품 ID가 주어질 때 상품 삭제를 하면 성공한다")
    @Test
    void givenValidProductId_whenDeleteProduct_thenSuccess() {
        // Given
        Long productId = 1L;
        UserDetails user = User.builder()
                .username("1")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Seller seller = Seller.builder()
                .name("테스트 판매자")
                .loginId("seller123")
                .password("password")
                .businessName("테스트 비즈니스")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구")
                .businessContactTelNum("02-1234-5678")
                .businessContactEmail("seller@test.com")
                .role(RoleType.SELLER)
                .build();
        ReflectionTestUtils.setField(seller, "id", 1L);

        Category category = Category.builder()
                .name("전자제품")
                .parent(null)
                .build();

        Product product = Product.builder()
                .name("테스트 상품")
                .description("테스트 설명")
                .price(10000)
                .stockQuantity(100)
                .category(category)
                .seller(seller)
                .build();
        ReflectionTestUtils.setField(product, "id", productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        productService.deleteProduct(productId, user);

        // Then
        // Product.delete() method sets isDeleted field and deletedAt
        // We verify the method executed without exception
        verify(productRepository, times(1)).findById(productId);
    }

    @DisplayName("이미 삭제된 상품을 삭제하려고 하면 예외가 발생한다")
    @Test
    void givenAlreadyDeletedProduct_whenDeleteProduct_thenThrowException() {
        // Given
        Long productId = 1L;
        UserDetails user = User.builder()
                .username("1")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Seller seller = Seller.builder()
                .name("테스트 판매자")
                .loginId("seller123")
                .password("password")
                .businessName("테스트 비즈니스")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구")
                .businessContactTelNum("02-1234-5678")
                .businessContactEmail("seller@test.com")
                .role(RoleType.SELLER)
                .build();
        ReflectionTestUtils.setField(seller, "id", 1L);

        Category category = Category.builder()
                .name("전자제품")
                .parent(null)
                .build();

        Product product = Product.builder()
                .name("테스트 상품")
                .description("테스트 설명")
                .price(10000)
                .stockQuantity(100)
                .category(category)
                .seller(seller)
                .build();
        ReflectionTestUtils.setField(product, "id", productId);
        product.delete(); // 미리 삭제 처리

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When & Then
        assertThatThrownBy(() -> productService.deleteProduct(productId, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.ALREADY_DELETED_PRODUCT);

        verify(productRepository, times(1)).findById(productId);
    }

    // ==================== searchProductList 테스트 ====================

    @DisplayName("검색 조건이 주어질 때 상품 목록 조회를 하면 성공한다")
    @Test
    void givenSearchInfo_whenSearchProductList_thenSuccess() {
        // Given
        SearchProductDto.Request searchInfo = new SearchProductDto.Request();
        List<SearchProductDto.Response> productList = Arrays.asList(
                SearchProductDto.Response.builder()
                        .productId(1L)
                        .productName("상품1")
                        .price(10000)
                        .stockQuantity(100)
                        .build(),
                SearchProductDto.Response.builder()
                        .productId(2L)
                        .productName("상품2")
                        .price(20000)
                        .stockQuantity(200)
                        .build()
        );
        Page<SearchProductDto.Response> expectedPage = new PageImpl<>(productList, PageRequest.of(0, 10), 2);

        when(productRepository.searchPageOrderByCreatedAtDesc(searchInfo)).thenReturn(expectedPage);

        // When
        Page<SearchProductDto.Response> result = productService.searchProductList(searchInfo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(productRepository, times(1)).searchPageOrderByCreatedAtDesc(searchInfo);
    }

    // ==================== findProductDetail 테스트 ====================

    @DisplayName("상품 ID가 주어질 때 상품 상세 조회를 하면 성공한다")
    @Test
    void givenProductId_whenFindProductDetail_thenSuccess() {
        // Given
        Long productId = 1L;
        FindProductDetailDto expectedDetail = FindProductDetailDto.builder()
                .productId(productId)
                .productName("테스트 상품")
                .productPrice(10000)
                .productStockQuantity(100)
                .productDescription("테스트 설명")
                .build();

        when(request.getCookies()).thenReturn(null);
        when(cookieUtils.setCookieExpire(eq(CookieCode.POST_VIEW), anyString(), anyInt()))
                .thenReturn(new Cookie("postView", "[1]"));
        when(redisUtil.hasKey(anyString())).thenReturn(false);
        when(productRepository.findProductDetail(productId)).thenReturn(expectedDetail);

        // When
        FindProductDetailDto result = productService.findProductDetail(productId, request, response);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getProductName()).isEqualTo("테스트 상품");

        verify(productRepository, times(1)).findProductDetail(productId);
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    // ==================== applyHitCount 테스트 ====================

    @DisplayName("상품 ID와 조회수가 주어질 때 조회수 적용을 하면 성공한다")
    @Test
    void givenProductIdAndHitCount_whenApplyHitCount_thenSuccess() {
        // Given
        Long productId = 1L;
        int hitCount = 10;

        Seller seller = Seller.builder()
                .name("테스트 판매자")
                .loginId("seller123")
                .password("password")
                .businessName("테스트 비즈니스")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구")
                .businessContactTelNum("02-1234-5678")
                .businessContactEmail("seller@test.com")
                .role(RoleType.SELLER)
                .build();

        Category category = Category.builder()
                .name("전자제품")
                .parent(null)
                .build();

        Product product = Product.builder()
                .name("테스트 상품")
                .description("테스트 설명")
                .price(10000)
                .stockQuantity(100)
                .category(category)
                .seller(seller)
                .build();
        ReflectionTestUtils.setField(product, "id", productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        productService.applyHitCount(productId, hitCount);

        // Then
        assertThat(product.getHitCount()).isEqualTo(hitCount);

        verify(productRepository, times(1)).findById(productId);
    }

    @DisplayName("존재하지 않는 상품 ID가 주어질 때 조회수 적용을 하면 예외가 발생한다")
    @Test
    void givenNonExistentProductId_whenApplyHitCount_thenThrowException() {
        // Given
        Long productId = 999L;
        int hitCount = 10;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.applyHitCount(productId, hitCount))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.PRODUCT_NOT_FOUND);

        verify(productRepository, times(1)).findById(productId);
    }

    // ==================== findProductById 테스트 ====================

    @DisplayName("존재하는 상품 ID가 주어질 때 조회를 하면 성공한다")
    @Test
    void givenExistingProductId_whenFindProductById_thenSuccess() {
        // Given
        Long productId = 1L;

        Seller seller = Seller.builder()
                .name("테스트 판매자")
                .loginId("seller123")
                .password("password")
                .businessName("테스트 비즈니스")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구")
                .businessContactTelNum("02-1234-5678")
                .businessContactEmail("seller@test.com")
                .role(RoleType.SELLER)
                .build();

        Category category = Category.builder()
                .name("전자제품")
                .parent(null)
                .build();

        Product product = Product.builder()
                .name("테스트 상품")
                .description("테스트 설명")
                .price(10000)
                .stockQuantity(100)
                .category(category)
                .seller(seller)
                .build();
        ReflectionTestUtils.setField(product, "id", productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        Product result = productService.findProductById(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("테스트 상품");

        verify(productRepository, times(1)).findById(productId);
    }

    @DisplayName("존재하지 않는 상품 ID가 주어질 때 조회를 하면 예외가 발생한다")
    @Test
    void givenNonExistentProductId_whenFindProductById_thenThrowException() {
        // Given
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.findProductById(productId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.PRODUCT_NOT_FOUND);

        verify(productRepository, times(1)).findById(productId);
    }

    // ==================== findProductByIdWithLock 테스트 ====================

    @DisplayName("존재하는 상품 ID가 주어질 때 락과 함께 조회를 하면 성공한다")
    @Test
    void givenExistingProductId_whenFindProductByIdWithLock_thenSuccess() {
        // Given
        Long productId = 1L;

        Seller seller = Seller.builder()
                .name("테스트 판매자")
                .loginId("seller123")
                .password("password")
                .businessName("테스트 비즈니스")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구")
                .businessContactTelNum("02-1234-5678")
                .businessContactEmail("seller@test.com")
                .role(RoleType.SELLER)
                .build();

        Category category = Category.builder()
                .name("전자제품")
                .parent(null)
                .build();

        Product product = Product.builder()
                .name("테스트 상품")
                .description("테스트 설명")
                .price(10000)
                .stockQuantity(100)
                .category(category)
                .seller(seller)
                .build();
        ReflectionTestUtils.setField(product, "id", productId);

        when(productRepository.findByIdWithLock(productId)).thenReturn(Optional.of(product));

        // When
        Product result = productService.findProductByIdWithLock(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);

        verify(productRepository, times(1)).findByIdWithLock(productId);
    }

    @DisplayName("존재하지 않는 상품 ID가 주어질 때 락과 함께 조회를 하면 예외가 발생한다")
    @Test
    void givenNonExistentProductId_whenFindProductByIdWithLock_thenThrowException() {
        // Given
        Long productId = 999L;
        when(productRepository.findByIdWithLock(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.findProductByIdWithLock(productId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.PRODUCT_NOT_FOUND);

        verify(productRepository, times(1)).findByIdWithLock(productId);
    }

    // ==================== findProductsByIdInWithLock 테스트 ====================

    @DisplayName("상품 ID 리스트가 주어질 때 락과 함께 조회를 하면 성공한다")
    @Test
    void givenProductIdList_whenFindProductsByIdInWithLock_thenSuccess() {
        // Given
        List<Long> productIds = Arrays.asList(1L, 2L, 3L);

        Seller seller = Seller.builder()
                .name("테스트 판매자")
                .loginId("seller123")
                .password("password")
                .businessName("테스트 비즈니스")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구")
                .businessContactTelNum("02-1234-5678")
                .businessContactEmail("seller@test.com")
                .role(RoleType.SELLER)
                .build();

        Category category = Category.builder()
                .name("전자제품")
                .parent(null)
                .build();

        List<Product> products = Arrays.asList(
                Product.builder().name("상품1").description("설명1").price(10000).stockQuantity(100).category(category).seller(seller).build(),
                Product.builder().name("상품2").description("설명2").price(20000).stockQuantity(200).category(category).seller(seller).build(),
                Product.builder().name("상품3").description("설명3").price(30000).stockQuantity(300).category(category).seller(seller).build()
        );

        when(productRepository.findByIdInWithLock(productIds)).thenReturn(products);

        // When
        List<Product> result = productService.findProductsByIdInWithLock(productIds);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);

        verify(productRepository, times(1)).findByIdInWithLock(productIds);
    }

    @DisplayName("빈 ID 리스트가 주어질 때 락과 함께 조회를 하면 빈 리스트를 반환한다")
    @Test
    void givenEmptyIdList_whenFindProductsByIdInWithLock_thenReturnEmptyList() {
        // Given
        List<Long> emptyIds = new ArrayList<>();

        // When
        List<Product> result = productService.findProductsByIdInWithLock(emptyIds);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(productRepository, never()).findByIdInWithLock(anyList());
    }

    @DisplayName("null ID 리스트가 주어질 때 락과 함께 조회를 하면 빈 리스트를 반환한다")
    @Test
    void givenNullIdList_whenFindProductsByIdInWithLock_thenReturnEmptyList() {
        // Given
        List<Long> nullIds = null;

        // When
        List<Product> result = productService.findProductsByIdInWithLock(nullIds);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(productRepository, never()).findByIdInWithLock(anyList());
    }
}
