package com.cookyuu.ecms_server.domain.cart.service;

import com.cookyuu.ecms_server.domain.cart.dto.DeleteCartItemDto;
import com.cookyuu.ecms_server.domain.cart.dto.UpdateCartItemDto;
import com.cookyuu.ecms_server.domain.cart.entity.Cart;
import com.cookyuu.ecms_server.domain.cart.entity.CartItem;
import com.cookyuu.ecms_server.domain.cart.logging.CartLogHelper;
import com.cookyuu.ecms_server.domain.cart.repository.CartItemRepository;
import com.cookyuu.ecms_server.domain.cart.repository.CartRepository;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.domain.product.entity.Category;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.domain.product.service.ProductService;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.utils.UserUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private ProductService productService;

    @Mock
    private CartLogHelper cartLogHelper;

    @Mock
    private UserUtils userUtils;

    @BeforeEach
    void setUp() {
        // Mock userUtils.getUserId to return Long from user.getUsername()
        lenient().when(userUtils.getUserId(any(UserDetails.class)))
            .thenAnswer(invocation -> {
                UserDetails user = invocation.getArgument(0);
                return Long.parseLong(user.getUsername());
            });
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @DisplayName("유효한 회원 정보가 주어질 때 장바구니를 생성하면 성공한다")
    @Test
    void givenValidMember_whenMakeCart_thenSuccess() {
        // Given
        Member member = createMember(1L, "testUser", "test@test.com");
        Cart cart = createCart(1L, member);

        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        cartService.makeCart(member);

        // Then
        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository, times(1)).save(cartCaptor.capture());
        Cart savedCart = cartCaptor.getValue();
        assertThat(savedCart.getMember()).isEqualTo(member);
    }

    @DisplayName("새로운 상품이 주어질 때 장바구니 아이템을 추가하면 성공한다")
    @Test
    void givenNewProduct_whenUpdateCartItem_thenAddSuccess() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Member member = createMember(userId, "testUser", "test@test.com");
        Seller seller = createSeller(1L, "seller", "seller@test.com");
        Category category = createCategory(1L, "테스트 카테고리");
        Product product = createProduct(1L, "테스트 상품", 10000, 100, category, seller);
        Cart cart = createCart(1L, member);

        UpdateCartItemDto.Request request = new UpdateCartItemDto.Request(product.getId(), 5);

        when(memberService.findMemberById(userId)).thenReturn(member);
        when(productService.findProductById(product.getId())).thenReturn(product);
        when(cartRepository.findByMemberId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.existsByCartAndProduct(cart, product)).thenReturn(false);

        // When
        cartService.updateCartItem(user, request);

        // Then
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @DisplayName("기존 상품이 주어질 때 장바구니 아이템을 수정하면 성공한다")
    @Test
    void givenExistingProduct_whenUpdateCartItem_thenUpdateSuccess() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Member member = createMember(userId, "testUser", "test@test.com");
        Seller seller = createSeller(1L, "seller", "seller@test.com");
        Category category = createCategory(1L, "테스트 카테고리");
        Product product = createProduct(1L, "테스트 상품", 10000, 100, category, seller);
        Cart cart = createCart(1L, member);
        CartItem cartItem = createCartItem(1L, cart, product, 3);

        UpdateCartItemDto.Request request = new UpdateCartItemDto.Request(product.getId(), 5);

        when(memberService.findMemberById(userId)).thenReturn(member);
        when(productService.findProductById(product.getId())).thenReturn(product);
        when(cartRepository.findByMemberId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.existsByCartAndProduct(cart, product)).thenReturn(true);
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(cartItem));

        // When
        cartService.updateCartItem(user, request);

        // Then
        assertThat(cartItem.getQuantity()).isEqualTo(5);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @DisplayName("삭제된 상품이 주어질 때 장바구니 아이템을 추가하면 예외가 발생한다")
    @Test
    void givenDeletedProduct_whenUpdateCartItem_thenThrowException() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Member member = createMember(userId, "testUser", "test@test.com");
        Seller seller = createSeller(1L, "seller", "seller@test.com");
        Category category = createCategory(1L, "테스트 카테고리");
        Product product = createProduct(1L, "테스트 상품", 10000, 100, category, seller);
        product.delete();
        Cart cart = createCart(1L, member);

        UpdateCartItemDto.Request request = new UpdateCartItemDto.Request(product.getId(), 5);

        when(memberService.findMemberById(userId)).thenReturn(member);
        when(productService.findProductById(product.getId())).thenReturn(product);

        // When & Then
        assertThatThrownBy(() -> cartService.updateCartItem(user, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.ALREADY_DELETED_PRODUCT);
    }

    @DisplayName("수량이 1보다 작을 때 장바구니 아이템을 추가하면 예외가 발생한다")
    @Test
    void givenQuantityLessThanOne_whenUpdateCartItem_thenThrowException() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Member member = createMember(userId, "testUser", "test@test.com");
        Seller seller = createSeller(1L, "seller", "seller@test.com");
        Category category = createCategory(1L, "테스트 카테고리");
        Product product = createProduct(1L, "테스트 상품", 10000, 100, category, seller);
        Cart cart = createCart(1L, member);

        UpdateCartItemDto.Request request = new UpdateCartItemDto.Request(product.getId(), 0);

        when(memberService.findMemberById(userId)).thenReturn(member);
        when(productService.findProductById(product.getId())).thenReturn(product);
        when(cartRepository.findByMemberId(userId)).thenReturn(Optional.of(cart));

        // When & Then
        assertThatThrownBy(() -> cartService.updateCartItem(user, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.BAD_REQUEST);
    }

    @DisplayName("장바구니가 존재하지 않을 때 아이템을 추가하면 예외가 발생한다")
    @Test
    void givenNoCart_whenUpdateCartItem_thenThrowException() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Member member = createMember(userId, "testUser", "test@test.com");
        Seller seller = createSeller(1L, "seller", "seller@test.com");
        Category category = createCategory(1L, "테스트 카테고리");
        Product product = createProduct(1L, "테스트 상품", 10000, 100, category, seller);

        UpdateCartItemDto.Request request = new UpdateCartItemDto.Request(product.getId(), 5);

        when(memberService.findMemberById(userId)).thenReturn(member);
        when(productService.findProductById(product.getId())).thenReturn(product);
        when(cartRepository.findByMemberId(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.updateCartItem(user, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.CART_NOT_FOUND);
    }

    @DisplayName("유효한 요청이 주어질 때 장바구니 아이템을 삭제하면 성공한다")
    @Test
    void givenValidRequest_whenDeleteCartItem_thenSuccess() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Member member = createMember(userId, "testUser", "test@test.com");
        Seller seller = createSeller(1L, "seller", "seller@test.com");
        Category category = createCategory(1L, "테스트 카테고리");
        Product product = createProduct(1L, "테스트 상품", 10000, 100, category, seller);
        Cart cart = createCart(1L, member);
        CartItem cartItem = createCartItem(1L, cart, product, 5);

        DeleteCartItemDto.Request request = new DeleteCartItemDto.Request(product.getId());

        when(memberService.findMemberById(userId)).thenReturn(member);
        when(productService.findProductById(product.getId())).thenReturn(product);
        when(cartRepository.findByMemberId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(cartItem));

        // When
        cartService.deleteCartItem(user, request);

        // Then
        verify(cartItemRepository, times(1)).delete(cartItem);
    }

    @DisplayName("장바구니가 존재하지 않을 때 아이템을 삭제하면 예외가 발생한다")
    @Test
    void givenNoCart_whenDeleteCartItem_thenThrowException() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Member member = createMember(userId, "testUser", "test@test.com");
        Seller seller = createSeller(1L, "seller", "seller@test.com");
        Category category = createCategory(1L, "테스트 카테고리");
        Product product = createProduct(1L, "테스트 상품", 10000, 100, category, seller);

        DeleteCartItemDto.Request request = new DeleteCartItemDto.Request(product.getId());

        when(memberService.findMemberById(userId)).thenReturn(member);
        when(productService.findProductById(product.getId())).thenReturn(product);
        when(cartRepository.findByMemberId(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.deleteCartItem(user, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.CART_NOT_FOUND);
    }

    @DisplayName("장바구니 아이템이 존재하지 않을 때 삭제하면 예외가 발생한다")
    @Test
    void givenNoCartItem_whenDeleteCartItem_thenThrowException() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Member member = createMember(userId, "testUser", "test@test.com");
        Seller seller = createSeller(1L, "seller", "seller@test.com");
        Category category = createCategory(1L, "테스트 카테고리");
        Product product = createProduct(1L, "테스트 상품", 10000, 100, category, seller);
        Cart cart = createCart(1L, member);

        DeleteCartItemDto.Request request = new DeleteCartItemDto.Request(product.getId());

        when(memberService.findMemberById(userId)).thenReturn(member);
        when(productService.findProductById(product.getId())).thenReturn(product);
        when(cartRepository.findByMemberId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.deleteCartItem(user, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.CARTITEM_NOT_FOUND);
    }

    @DisplayName("장바구니와 상품이 주어질 때 아이템을 삭제하면 성공한다")
    @Test
    void givenCartAndProduct_whenDeleteCartItem_thenSuccess() {
        // Given
        Member member = createMember(1L, "testUser", "test@test.com");
        Seller seller = createSeller(1L, "seller", "seller@test.com");
        Category category = createCategory(1L, "테스트 카테고리");
        Product product = createProduct(1L, "테스트 상품", 10000, 100, category, seller);
        Cart cart = createCart(1L, member);
        CartItem cartItem = createCartItem(1L, cart, product, 5);

        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(cartItem));

        // When
        cartService.deleteCartItem(cart, product);

        // Then
        verify(cartItemRepository, times(1)).delete(cartItem);
    }

    @DisplayName("유효한 회원 ID가 주어질 때 장바구니를 조회하면 성공한다")
    @Test
    void givenValidMemberId_whenFindCartByMemberId_thenSuccess() {
        // Given
        Long memberId = 1L;
        Member member = createMember(memberId, "testUser", "test@test.com");
        Cart cart = createCart(1L, member);

        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(cart));

        // When
        Cart result = cartService.findCartByMemberId(memberId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMember().getId()).isEqualTo(memberId);
        verify(cartRepository, times(1)).findByMemberId(memberId);
    }

    @DisplayName("존재하지 않는 회원 ID가 주어질 때 장바구니를 조회하면 예외가 발생한다")
    @Test
    void givenNonExistentMemberId_whenFindCartByMemberId_thenThrowException() {
        // Given
        Long memberId = 999L;

        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.findCartByMemberId(memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.CART_NOT_FOUND);
    }

    @DisplayName("유효한 회원 ID가 주어질 때 장바구니와 아이템, 상품을 함께 조회하면 성공한다")
    @Test
    void givenValidMemberId_whenFindCartByMemberIdWithCartItemsAndProducts_thenSuccess() {
        // Given
        Long memberId = 1L;
        Member member = createMember(memberId, "testUser", "test@test.com");
        Cart cart = createCart(1L, member);

        when(cartRepository.findByMemberIdWithCartItemsAndProducts(memberId)).thenReturn(Optional.of(cart));

        // When
        Cart result = cartService.findCartByMemberIdWithCartItemsAndProducts(memberId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMember().getId()).isEqualTo(memberId);
        verify(cartRepository, times(1)).findByMemberIdWithCartItemsAndProducts(memberId);
    }

    @DisplayName("존재하지 않는 회원 ID가 주어질 때 장바구니와 아이템을 조회하면 예외가 발생한다")
    @Test
    void givenNonExistentMemberId_whenFindCartByMemberIdWithCartItemsAndProducts_thenThrowException() {
        // Given
        Long memberId = 999L;

        when(cartRepository.findByMemberIdWithCartItemsAndProducts(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.findCartByMemberIdWithCartItemsAndProducts(memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.CART_NOT_FOUND);
    }

    // Helper methods
    private Member createMember(Long id, String loginId, String email) {
        Member member = Member.builder()
                .loginId(loginId)
                .password("encodedPassword")
                .email(email)
                .name("테스트 회원")
                .phoneNumber("010-1234-5678")
                .role(RoleType.USER)
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private Seller createSeller(Long id, String loginId, String email) {
        Seller seller = Seller.builder()
                .loginId(loginId)
                .password("encodedPassword")
                .businessNumber("101-81-00155")
                .businessContactEmail(email)
                .businessContactTelNum("010-1234-5678")
                .build();
        ReflectionTestUtils.setField(seller, "id", id);
        return seller;
    }

    private Category createCategory(Long id, String name) {
        Category category = Category.builder()
                .name(name)
                .build();
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    private Product createProduct(Long id, String name, Integer price, Integer stockQuantity, Category category, Seller seller) {
        Product product = Product.builder()
                .name(name)
                .description("테스트 설명")
                .price(price)
                .stockQuantity(stockQuantity)
                .category(category)
                .seller(seller)
                .build();
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private Cart createCart(Long id, Member member) {
        Cart cart = Cart.builder()
                .member(member)
                .cartItems(new ArrayList<>())
                .build();
        ReflectionTestUtils.setField(cart, "id", id);
        return cart;
    }

    private CartItem createCartItem(Long id, Cart cart, Product product, Integer quantity) {
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .build();
        ReflectionTestUtils.setField(cartItem, "id", id);
        return cartItem;
    }
}
