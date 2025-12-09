package com.cookyuu.ecms_server.domain.order.service;

import com.cookyuu.ecms_server.domain.cart.entity.Cart;
import com.cookyuu.ecms_server.domain.cart.entity.CartItem;
import com.cookyuu.ecms_server.domain.cart.service.CartService;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.domain.order.dto.*;
import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.domain.order.entity.OrderLine;
import com.cookyuu.ecms_server.domain.order.enums.OrderStatus;
import com.cookyuu.ecms_server.domain.order.repository.OrderLineRepository;
import com.cookyuu.ecms_server.domain.order.repository.OrderRepository;
import com.cookyuu.ecms_server.domain.product.entity.Category;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.domain.product.service.ProductService;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import com.cookyuu.ecms_server.common.enums.RedisKeyCode;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.generator.BusinessNumberGenerator;
import com.cookyuu.ecms_server.common.security.service.AuthorizationService;
import com.cookyuu.ecms_server.common.utils.RedisUtils;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderLineRepository orderLineRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private CartService cartService;

    @Mock
    private ProductService productService;

    @Mock
    private RedisUtils redisUtils;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private BusinessNumberGenerator businessNumberGenerator;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @DisplayName("유효한 주문 정보가 주어질 때 주문을 생성하면 성공한다")
    @Test
    void givenValidOrderInfo_whenCreateOrder_thenSuccess() {
        // Given
        Long userId = 1L;
        Member buyer = createMember(userId, "buyer", "buyer@test.com");

        Seller seller = createSeller(1L, "seller", "seller@test.com");
        Category category = createCategory(1L, "테스트 카테고리");
        Product product = createProduct(1L, "테스트 상품", 10000, 100, category, seller);

        Cart cart = createCart(1L, buyer);
        CartItem cartItem = createCartItem(1L, cart, product, 5);
        cart.addCartItem(cartItem);

        CreateOrderItemInfo orderItem = CreateOrderItemInfo.builder()
                .productId(product.getId())
                .price(10000)
                .quantity(5)
                .build();

        OrderShipmentInfo shipmentInfo = OrderShipmentInfo.builder()
                .destination("서울시 강남구")
                .destinationDetail("101동 101호")
                .recipientName("수령인")
                .recipientPhoneNumber("010-1234-5678")
                .build();

        CreateOrderDto.Request request = CreateOrderDto.Request.builder()
                .orderItemList(Arrays.asList(orderItem))
                .shipmentInfo(shipmentInfo)
                .build();

        Order savedOrder = Order.builder()
                .totalPrice(50000)
                .orderNumber("TEST_ORDER_NUMBER")
                .status(OrderStatus.ORDER_COMPLETE)
                .buyer(buyer)
                .destination(shipmentInfo.getDestination())
                .destinationDetail(shipmentInfo.getDestinationDetail())
                .recipientName(shipmentInfo.getRecipientName())
                .recipientPhoneNumber(shipmentInfo.getRecipientPhoneNumber())
                .build();
        ReflectionTestUtils.setField(savedOrder, "id", 1L);

        when(memberService.findMemberById(userId)).thenReturn(buyer);
        when(cartService.findCartByMemberIdWithCartItemsAndProducts(userId)).thenReturn(cart);
        when(productService.findProductsByIdInWithLock(anyList())).thenReturn(Arrays.asList(product));
        when(businessNumberGenerator.generateOrderNumber(any(), any())).thenReturn("TEST_ORDER_NUMBER");
        when(redisUtils.getData(anyString())).thenReturn(null);
        doNothing().when(redisUtils).setDataExpire(anyString(), anyString(), anyLong());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderLineRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // When
        CreateOrderDto.Response response = orderService.createOrder(userId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderNumber()).isEqualTo("TEST_ORDER_NUMBER");
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderLineRepository, times(1)).saveAll(anyList());
        assertThat(product.getStockQuantity()).isEqualTo(95);
    }

    @DisplayName("존재하지 않는 상품 ID가 주어질 때 주문을 생성하면 예외가 발생한다")
    @Test
    void givenNonExistentProductId_whenCreateOrder_thenThrowException() {
        // Given
        Long userId = 1L;
        Member buyer = createMember(userId, "buyer", "buyer@test.com");
        Cart cart = createCart(1L, buyer);

        CreateOrderItemInfo orderItem = CreateOrderItemInfo.builder()
                .productId(999L)
                .price(10000)
                .quantity(5)
                .build();

        OrderShipmentInfo shipmentInfo = OrderShipmentInfo.builder()
                .destination("서울시 강남구")
                .build();

        CreateOrderDto.Request request = CreateOrderDto.Request.builder()
                .orderItemList(Arrays.asList(orderItem))
                .shipmentInfo(shipmentInfo)
                .build();

        when(memberService.findMemberById(userId)).thenReturn(buyer);
        when(cartService.findCartByMemberIdWithCartItemsAndProducts(userId)).thenReturn(cart);
        when(productService.findProductsByIdInWithLock(anyList())).thenReturn(new ArrayList<>());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.PRODUCT_NOT_FOUND);
    }

    @DisplayName("삭제된 상품이 주어질 때 주문을 생성하면 예외가 발생한다")
    @Test
    void givenDeletedProduct_whenCreateOrder_thenThrowException() {
        // Given
        Long userId = 1L;
        Member buyer = createMember(userId, "buyer", "buyer@test.com");

        Seller seller = createSeller(1L, "seller", "seller@test.com");
        Category category = createCategory(1L, "테스트 카테고리");
        Product product = createProduct(1L, "테스트 상품", 10000, 100, category, seller);
        product.delete();

        Cart cart = createCart(1L, buyer);

        CreateOrderItemInfo orderItem = CreateOrderItemInfo.builder()
                .productId(product.getId())
                .price(10000)
                .quantity(5)
                .build();

        OrderShipmentInfo shipmentInfo = OrderShipmentInfo.builder()
                .destination("서울시 강남구")
                .build();

        CreateOrderDto.Request request = CreateOrderDto.Request.builder()
                .orderItemList(Arrays.asList(orderItem))
                .shipmentInfo(shipmentInfo)
                .build();

        when(memberService.findMemberById(userId)).thenReturn(buyer);
        when(cartService.findCartByMemberIdWithCartItemsAndProducts(userId)).thenReturn(cart);
        when(productService.findProductsByIdInWithLock(anyList())).thenReturn(Arrays.asList(product));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.ALREADY_DELETED_PRODUCT);
    }

    @DisplayName("재고가 0인 상품이 주어질 때 주문을 생성하면 예외가 발생한다")
    @Test
    void givenProductWithZeroStock_whenCreateOrder_thenThrowException() {
        // Given
        Long userId = 1L;
        Member buyer = createMember(userId, "buyer", "buyer@test.com");

        Seller seller = createSeller(1L, "seller", "seller@test.com");
        Category category = createCategory(1L, "테스트 카테고리");
        Product product = createProduct(1L, "테스트 상품", 10000, 0, category, seller);

        Cart cart = createCart(1L, buyer);

        CreateOrderItemInfo orderItem = CreateOrderItemInfo.builder()
                .productId(product.getId())
                .price(10000)
                .quantity(1)
                .build();

        OrderShipmentInfo shipmentInfo = OrderShipmentInfo.builder()
                .destination("서울시 강남구")
                .build();

        CreateOrderDto.Request request = CreateOrderDto.Request.builder()
                .orderItemList(Arrays.asList(orderItem))
                .shipmentInfo(shipmentInfo)
                .build();

        when(memberService.findMemberById(userId)).thenReturn(buyer);
        when(cartService.findCartByMemberIdWithCartItemsAndProducts(userId)).thenReturn(cart);
        when(productService.findProductsByIdInWithLock(anyList())).thenReturn(Arrays.asList(product));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.PRODUCT_SOLD_OUT);
    }

    @DisplayName("주문 수량이 재고보다 많을 때 주문을 생성하면 예외가 발생한다")
    @Test
    void givenQuantityExceedsStock_whenCreateOrder_thenThrowException() {
        // Given
        Long userId = 1L;
        Member buyer = createMember(userId, "buyer", "buyer@test.com");

        Seller seller = createSeller(1L, "seller", "seller@test.com");
        Category category = createCategory(1L, "테스트 카테고리");
        Product product = createProduct(1L, "테스트 상품", 10000, 10, category, seller);

        Cart cart = createCart(1L, buyer);

        CreateOrderItemInfo orderItem = CreateOrderItemInfo.builder()
                .productId(product.getId())
                .price(10000)
                .quantity(20)
                .build();

        OrderShipmentInfo shipmentInfo = OrderShipmentInfo.builder()
                .destination("서울시 강남구")
                .build();

        CreateOrderDto.Request request = CreateOrderDto.Request.builder()
                .orderItemList(Arrays.asList(orderItem))
                .shipmentInfo(shipmentInfo)
                .build();

        when(memberService.findMemberById(userId)).thenReturn(buyer);
        when(cartService.findCartByMemberIdWithCartItemsAndProducts(userId)).thenReturn(cart);
        when(productService.findProductsByIdInWithLock(anyList())).thenReturn(Arrays.asList(product));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.PRODUCT_SOLD_OUT);
    }

    @DisplayName("주문 가격과 현재 가격이 다를 때 주문을 생성하면 예외가 발생한다")
    @Test
    void givenPriceMismatch_whenCreateOrder_thenThrowException() {
        // Given
        Long userId = 1L;
        Member buyer = createMember(userId, "buyer", "buyer@test.com");

        Seller seller = createSeller(1L, "seller", "seller@test.com");
        Category category = createCategory(1L, "테스트 카테고리");
        Product product = createProduct(1L, "테스트 상품", 10000, 100, category, seller);

        Cart cart = createCart(1L, buyer);

        CreateOrderItemInfo orderItem = CreateOrderItemInfo.builder()
                .productId(product.getId())
                .price(9000)
                .quantity(5)
                .build();

        OrderShipmentInfo shipmentInfo = OrderShipmentInfo.builder()
                .destination("서울시 강남구")
                .build();

        CreateOrderDto.Request request = CreateOrderDto.Request.builder()
                .orderItemList(Arrays.asList(orderItem))
                .shipmentInfo(shipmentInfo)
                .build();

        when(memberService.findMemberById(userId)).thenReturn(buyer);
        when(cartService.findCartByMemberIdWithCartItemsAndProducts(userId)).thenReturn(cart);
        when(productService.findProductsByIdInWithLock(anyList())).thenReturn(Arrays.asList(product));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.ORDER_PROCESS_FAIL);
    }

    @DisplayName("유효한 취소 요청이 주어질 때 주문을 취소하면 성공한다")
    @Test
    void givenValidCancelRequest_whenCancelOrder_thenSuccess() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Member buyer = createMember(userId, "buyer", "buyer@test.com");
        Seller seller = createSeller(1L, "seller", "seller@test.com");
        Category category = createCategory(1L, "테스트 카테고리");
        Product product = createProduct(1L, "테스트 상품", 10000, 100, category, seller);

        Order order = Order.builder()
                .totalPrice(50000)
                .orderNumber("TEST_ORDER_NUMBER")
                .status(OrderStatus.ORDER_COMPLETE)
                .buyer(buyer)
                .destination("서울시 강남구")
                .build();
        ReflectionTestUtils.setField(order, "id", 1L);

        OrderLine orderLine = OrderLine.builder()
                .order(order)
                .product(product)
                .quantity(5)
                .price(10000)
                .productName("테스트 상품")
                .build();
        order.getOrderLines().add(orderLine);

        CancelOrderDto.Request cancelRequest = new CancelOrderDto.Request("TEST_ORDER_NUMBER", "단순 변심");

        when(orderRepository.findByOrderNumberWithProductsForUpdate(anyString())).thenReturn(Optional.of(order));
        doNothing().when(authorizationService).validateResourceOwnership(anyLong(), anyLong(), any(ResultCode.class));

        // When
        ResultCode result = orderService.cancelOrder(user, cancelRequest);

        // Then
        assertThat(result).isEqualTo(ResultCode.ORDER_CANCEL_SUCCESS);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(product.getStockQuantity()).isEqualTo(105);
    }

    @DisplayName("이미 취소된 주문이 주어질 때 취소를 시도하면 예외가 발생한다")
    @Test
    void givenAlreadyCanceledOrder_whenCancelOrder_thenThrowException() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Member buyer = createMember(userId, "buyer", "buyer@test.com");

        Order order = Order.builder()
                .totalPrice(50000)
                .orderNumber("TEST_ORDER_NUMBER")
                .status(OrderStatus.CANCELED)
                .buyer(buyer)
                .destination("서울시 강남구")
                .build();
        ReflectionTestUtils.setField(order, "id", 1L);
        order.cancel("이전 취소");

        CancelOrderDto.Request cancelRequest = new CancelOrderDto.Request("TEST_ORDER_NUMBER", "단순 변심");

        when(orderRepository.findByOrderNumberWithProductsForUpdate(anyString())).thenReturn(Optional.of(order));

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(user, cancelRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.ALREADY_CANCELED_ORDER);
    }

    @DisplayName("취소 불가능한 상태의 주문이 주어질 때 취소를 시도하면 예외가 발생한다")
    @Test
    void givenNonCancelableOrder_whenCancelOrder_thenThrowException() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Member buyer = createMember(userId, "buyer", "buyer@test.com");

        Order order = Order.builder()
                .totalPrice(50000)
                .orderNumber("TEST_ORDER_NUMBER")
                .status(OrderStatus.SHIPPING)
                .buyer(buyer)
                .destination("서울시 강남구")
                .build();
        ReflectionTestUtils.setField(order, "id", 1L);

        CancelOrderDto.Request cancelRequest = new CancelOrderDto.Request("TEST_ORDER_NUMBER", "단순 변심");

        when(orderRepository.findByOrderNumberWithProductsForUpdate(anyString())).thenReturn(Optional.of(order));
        doNothing().when(authorizationService).validateResourceOwnership(anyLong(), anyLong(), any(ResultCode.class));

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(user, cancelRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.ORDER_CANCEL_FAIL);
    }

    @DisplayName("다른 사용자의 주문이 주어질 때 취소를 시도하면 예외가 발생한다")
    @Test
    void givenOtherUserOrder_whenCancelOrder_thenThrowException() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Member buyer = createMember(2L, "other", "other@test.com");

        Order order = Order.builder()
                .totalPrice(50000)
                .orderNumber("TEST_ORDER_NUMBER")
                .status(OrderStatus.ORDER_COMPLETE)
                .buyer(buyer)
                .destination("서울시 강남구")
                .build();
        ReflectionTestUtils.setField(order, "id", 1L);

        CancelOrderDto.Request cancelRequest = new CancelOrderDto.Request("TEST_ORDER_NUMBER", "단순 변심");

        when(orderRepository.findByOrderNumberWithProductsForUpdate(anyString())).thenReturn(Optional.of(order));
        doThrow(new BusinessException(ResultCode.ORDER_BUYER_UNMATCHED))
                .when(authorizationService).validateResourceOwnership(eq(userId), eq(2L), any(ResultCode.class));

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(user, cancelRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.ORDER_BUYER_UNMATCHED);
    }

    @DisplayName("유효한 수정 요청이 주어질 때 주문을 수정하면 성공한다")
    @Test
    void givenValidReviseRequest_whenReviseOrder_thenSuccess() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Member buyer = createMember(userId, "buyer", "buyer@test.com");
        Seller seller = createSeller(1L, "seller", "seller@test.com");
        Category category = createCategory(1L, "테스트 카테고리");
        Product oldProduct = createProduct(1L, "기존 상품", 10000, 100, category, seller);
        Product newProduct = createProduct(2L, "새 상품", 15000, 50, category, seller);

        Order order = Order.builder()
                .totalPrice(50000)
                .orderNumber("TEST_ORDER_NUMBER")
                .status(OrderStatus.ORDER_COMPLETE)
                .buyer(buyer)
                .destination("서울시 강남구")
                .build();
        ReflectionTestUtils.setField(order, "id", 1L);

        OrderLine oldOrderLine = OrderLine.builder()
                .order(order)
                .product(oldProduct)
                .quantity(5)
                .price(10000)
                .productName("기존 상품")
                .build();
        order.getOrderLines().add(oldOrderLine);

        ReviseOrderItemInfo newOrderItem = new ReviseOrderItemInfo(newProduct.getId(), 15000, 3, null);
        ReviseOrderDto.Request reviseRequest = new ReviseOrderDto.Request("TEST_ORDER_NUMBER", Arrays.asList(newOrderItem), 0);

        when(orderRepository.findByOrderNumberWithProductsForUpdate(anyString())).thenReturn(Optional.of(order));
        when(productService.findProductsByIdInWithLock(anyList()))
                .thenReturn(Arrays.asList(oldProduct))
                .thenReturn(Arrays.asList(newProduct));
        doNothing().when(orderLineRepository).deleteAll(anyList());
        when(orderLineRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        doNothing().when(authorizationService).validateResourceOwnership(anyLong(), anyLong(), any(ResultCode.class));

        // When
        ResultCode result = orderService.reviseOrder(user, reviseRequest);

        // Then
        assertThat(result).isEqualTo(ResultCode.ORDER_REVISE_SUCCESS);
        assertThat(order.getTotalPrice()).isEqualTo(45000);
        assertThat(oldProduct.getStockQuantity()).isEqualTo(105);
        assertThat(newProduct.getStockQuantity()).isEqualTo(47);
        verify(orderLineRepository, times(1)).deleteAll(anyList());
        verify(orderLineRepository, times(1)).saveAll(anyList());
    }

    @DisplayName("이미 취소된 주문이 주어질 때 수정을 시도하면 예외가 발생한다")
    @Test
    void givenAlreadyCanceledOrder_whenReviseOrder_thenThrowException() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Member buyer = createMember(userId, "buyer", "buyer@test.com");

        Order order = Order.builder()
                .totalPrice(50000)
                .orderNumber("TEST_ORDER_NUMBER")
                .status(OrderStatus.CANCELED)
                .buyer(buyer)
                .destination("서울시 강남구")
                .build();
        ReflectionTestUtils.setField(order, "id", 1L);
        order.cancel("이전 취소");

        ReviseOrderItemInfo newOrderItem = new ReviseOrderItemInfo(1L, 15000, 3, null);
        ReviseOrderDto.Request reviseRequest = new ReviseOrderDto.Request("TEST_ORDER_NUMBER", Arrays.asList(newOrderItem), 0);

        when(orderRepository.findByOrderNumberWithProductsForUpdate(anyString())).thenReturn(Optional.of(order));

        // When & Then
        assertThatThrownBy(() -> orderService.reviseOrder(user, reviseRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.ALREADY_CANCELED_ORDER);
    }

    @DisplayName("수정 불가능한 상태의 주문이 주어질 때 수정을 시도하면 예외가 발생한다")
    @Test
    void givenNonRevisableOrder_whenReviseOrder_thenThrowException() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Member buyer = createMember(userId, "buyer", "buyer@test.com");

        Order order = Order.builder()
                .totalPrice(50000)
                .orderNumber("TEST_ORDER_NUMBER")
                .status(OrderStatus.SHIPPING)
                .buyer(buyer)
                .destination("서울시 강남구")
                .build();
        ReflectionTestUtils.setField(order, "id", 1L);

        ReviseOrderItemInfo newOrderItem = new ReviseOrderItemInfo(1L, 15000, 3, null);
        ReviseOrderDto.Request reviseRequest = new ReviseOrderDto.Request("TEST_ORDER_NUMBER", Arrays.asList(newOrderItem), 0);

        when(orderRepository.findByOrderNumberWithProductsForUpdate(anyString())).thenReturn(Optional.of(order));

        // When & Then
        assertThatThrownBy(() -> orderService.reviseOrder(user, reviseRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.ORDER_CANCEL_FAIL);
    }

    @DisplayName("존재하지 않는 상품으로 수정할 때 예외가 발생한다")
    @Test
    void givenNonExistentProductInRevision_whenReviseOrder_thenThrowException() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Member buyer = createMember(userId, "buyer", "buyer@test.com");
        Seller seller = createSeller(1L, "seller", "seller@test.com");
        Category category = createCategory(1L, "테스트 카테고리");
        Product oldProduct = createProduct(1L, "기존 상품", 10000, 100, category, seller);

        Order order = Order.builder()
                .totalPrice(50000)
                .orderNumber("TEST_ORDER_NUMBER")
                .status(OrderStatus.ORDER_COMPLETE)
                .buyer(buyer)
                .destination("서울시 강남구")
                .build();
        ReflectionTestUtils.setField(order, "id", 1L);

        OrderLine oldOrderLine = OrderLine.builder()
                .order(order)
                .product(oldProduct)
                .quantity(5)
                .price(10000)
                .productName("기존 상품")
                .build();
        order.getOrderLines().add(oldOrderLine);

        ReviseOrderItemInfo newOrderItem = new ReviseOrderItemInfo(999L, 15000, 3, null);
        ReviseOrderDto.Request reviseRequest = new ReviseOrderDto.Request("TEST_ORDER_NUMBER", Arrays.asList(newOrderItem), 0);

        when(orderRepository.findByOrderNumberWithProductsForUpdate(anyString())).thenReturn(Optional.of(order));
        when(productService.findProductsByIdInWithLock(anyList()))
                .thenReturn(Arrays.asList(oldProduct))
                .thenReturn(new ArrayList<>());
        doNothing().when(authorizationService).validateResourceOwnership(anyLong(), anyLong(), any(ResultCode.class));

        // When & Then
        assertThatThrownBy(() -> orderService.reviseOrder(user, reviseRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.PRODUCT_NOT_FOUND);
    }

    @DisplayName("다른 사용자의 주문이 주어질 때 수정을 시도하면 예외가 발생한다")
    @Test
    void givenOtherUserOrder_whenReviseOrder_thenThrowException() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Member buyer = createMember(2L, "other", "other@test.com");

        Order order = Order.builder()
                .totalPrice(50000)
                .orderNumber("TEST_ORDER_NUMBER")
                .status(OrderStatus.ORDER_COMPLETE)
                .buyer(buyer)
                .destination("서울시 강남구")
                .build();
        ReflectionTestUtils.setField(order, "id", 1L);

        ReviseOrderItemInfo newOrderItem = new ReviseOrderItemInfo(1L, 15000, 3, null);
        ReviseOrderDto.Request reviseRequest = new ReviseOrderDto.Request("TEST_ORDER_NUMBER", Arrays.asList(newOrderItem), 0);

        when(orderRepository.findByOrderNumberWithProductsForUpdate(anyString())).thenReturn(Optional.of(order));
        doThrow(new BusinessException(ResultCode.ORDER_BUYER_UNMATCHED))
                .when(authorizationService).validateResourceOwnership(eq(userId), eq(2L), any(ResultCode.class));

        // When & Then
        assertThatThrownBy(() -> orderService.reviseOrder(user, reviseRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.ORDER_BUYER_UNMATCHED);
    }

    @DisplayName("유효한 검색 조건이 주어질 때 주문 목록을 조회하면 성공한다")
    @Test
    void givenValidSearchRequest_whenSearchOrderList_thenSuccess() {
        // Given
        SearchOrderDto.Request searchRequest = SearchOrderDto.Request.builder()
                .option("orderNumber")
                .keyword("TEST")
                .pageable(PageRequest.of(0, 10))
                .build();

        SearchOrderDto.Response orderResponse = SearchOrderDto.Response.builder()
                .orderId(1L)
                .orderNumber("TEST_ORDER_NUMBER")
                .totalPrice(50000)
                .status(OrderStatus.ORDER_COMPLETE)
                .destination("서울시 강남구")
                .recipientName("수령인")
                .build();

        Page<SearchOrderDto.Response> expectedPage = new PageImpl<>(Arrays.asList(orderResponse));

        when(orderRepository.searchPageOrderByCreatedAtDesc(searchRequest)).thenReturn(expectedPage);

        // When
        Page<SearchOrderDto.Response> result = orderService.searchOrderList(searchRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getOrderNumber()).isEqualTo("TEST_ORDER_NUMBER");
        verify(orderRepository, times(1)).searchPageOrderByCreatedAtDesc(searchRequest);
    }

    @DisplayName("USER 권한의 본인 주문이 주어질 때 주문 상세를 조회하면 성공한다")
    @Test
    void givenUserRoleAndOwnOrder_whenGetOrderDetailCacheable_thenSuccess() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.singletonList(() -> "ROLE_USER"))
                .build();

        String orderNumber = "TEST_ORDER_NUMBER";

        OrderDetailDto.OrderInfo orderInfo = new OrderDetailDto.OrderInfo(
                orderNumber, 50000, OrderStatus.ORDER_COMPLETE,
                "서울시 강남구", "101동", "수령인", "010-1234-5678",
                userId, "buyer", "구매자", null
        );

        OrderDetailDto.OrderLineInfo orderLineInfo = new OrderDetailDto.OrderLineInfo(
                1L, "테스트 상품", 5, 10000, 1L, "판매자"
        );

        OrderDetailDto orderDetail = OrderDetailDto.builder()
                .orderInfo(orderInfo)
                .orderLines(Arrays.asList(orderLineInfo))
                .build();

        when(orderRepository.getOrderDetail(orderNumber)).thenReturn(orderDetail);
        when(authorizationService.getUserRole(user)).thenReturn(RoleType.USER);
        doNothing().when(authorizationService).validateOrderBuyerAccess(user, userId);

        // When
        OrderDetailDto result = orderService.getOrderDetailCacheable(user, orderNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderInfo().getOrderNumber()).isEqualTo(orderNumber);
        assertThat(result.getOrderLines()).hasSize(1);
        verify(orderRepository, times(1)).getOrderDetail(orderNumber);
    }

    @DisplayName("SELLER 권한의 판매자가 자신의 상품이 포함된 주문을 조회하면 성공한다")
    @Test
    void givenSellerRoleAndOwnProduct_whenGetOrderDetailCacheable_thenSuccess() {
        // Given
        Long sellerId = 1L;
        UserDetails user = User.builder()
                .username(sellerId.toString())
                .password("password")
                .authorities(Collections.singletonList(() -> "ROLE_SELLER"))
                .build();

        String orderNumber = "TEST_ORDER_NUMBER";

        OrderDetailDto.OrderInfo orderInfo = new OrderDetailDto.OrderInfo(
                orderNumber, 50000, OrderStatus.ORDER_COMPLETE,
                "서울시 강남구", "101동", "수령인", "010-1234-5678",
                2L, "buyer", "구매자", null
        );

        OrderDetailDto.OrderLineInfo orderLineInfo = new OrderDetailDto.OrderLineInfo(
                1L, "테스트 상품", 5, 10000, sellerId, "판매자"
        );

        OrderDetailDto orderDetail = OrderDetailDto.builder()
                .orderInfo(orderInfo)
                .orderLines(Arrays.asList(orderLineInfo))
                .build();

        when(orderRepository.getOrderDetail(orderNumber)).thenReturn(orderDetail);
        when(authorizationService.getUserRole(user)).thenReturn(RoleType.SELLER);
        doNothing().when(authorizationService).validateOrderSellerAccess(eq(user), anyList());

        // When
        OrderDetailDto result = orderService.getOrderDetailCacheable(user, orderNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderInfo().getOrderNumber()).isEqualTo(orderNumber);
        verify(orderRepository, times(1)).getOrderDetail(orderNumber);
    }

    @DisplayName("USER 권한의 다른 사용자 주문이 주어질 때 조회하면 예외가 발생한다")
    @Test
    void givenUserRoleAndOtherUserOrder_whenGetOrderDetailCacheable_thenThrowException() {
        // Given
        Long userId = 1L;
        UserDetails user = User.builder()
                .username(userId.toString())
                .password("password")
                .authorities(Collections.singletonList(() -> "ROLE_USER"))
                .build();

        String orderNumber = "TEST_ORDER_NUMBER";

        OrderDetailDto.OrderInfo orderInfo = new OrderDetailDto.OrderInfo(
                orderNumber, 50000, OrderStatus.ORDER_COMPLETE,
                "서울시 강남구", "101동", "수령인", "010-1234-5678",
                2L, "other", "다른사용자", null
        );

        OrderDetailDto.OrderLineInfo orderLineInfo = new OrderDetailDto.OrderLineInfo(
                1L, "테스트 상품", 5, 10000, 3L, "판매자"
        );

        OrderDetailDto orderDetail = OrderDetailDto.builder()
                .orderInfo(orderInfo)
                .orderLines(Arrays.asList(orderLineInfo))
                .build();

        when(orderRepository.getOrderDetail(orderNumber)).thenReturn(orderDetail);
        when(authorizationService.getUserRole(user)).thenReturn(RoleType.USER);
        doThrow(new BusinessException(ResultCode.ORDER_BUYER_UNMATCHED))
                .when(authorizationService).validateOrderBuyerAccess(user, 2L);

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderDetailCacheable(user, orderNumber))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.ORDER_BUYER_UNMATCHED);
    }

    @DisplayName("SELLER 권한의 판매자가 자신의 상품이 포함되지 않은 주문을 조회하면 예외가 발생한다")
    @Test
    void givenSellerRoleAndNoOwnProduct_whenGetOrderDetailCacheable_thenThrowException() {
        // Given
        Long sellerId = 1L;
        UserDetails user = User.builder()
                .username(sellerId.toString())
                .password("password")
                .authorities(Collections.singletonList(() -> "ROLE_SELLER"))
                .build();

        String orderNumber = "TEST_ORDER_NUMBER";

        OrderDetailDto.OrderInfo orderInfo = new OrderDetailDto.OrderInfo(
                orderNumber, 50000, OrderStatus.ORDER_COMPLETE,
                "서울시 강남구", "101동", "수령인", "010-1234-5678",
                2L, "buyer", "구매자", null
        );

        OrderDetailDto.OrderLineInfo orderLineInfo = new OrderDetailDto.OrderLineInfo(
                1L, "테스트 상품", 5, 10000, 3L, "다른판매자"
        );

        OrderDetailDto orderDetail = OrderDetailDto.builder()
                .orderInfo(orderInfo)
                .orderLines(Arrays.asList(orderLineInfo))
                .build();

        when(orderRepository.getOrderDetail(orderNumber)).thenReturn(orderDetail);
        when(authorizationService.getUserRole(user)).thenReturn(RoleType.SELLER);
        doThrow(new BusinessException(ResultCode.ORDER_SELLER_UNMATCHED))
                .when(authorizationService).validateOrderSellerAccess(eq(user), anyList());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderDetailCacheable(user, orderNumber))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.ORDER_SELLER_UNMATCHED);
    }

    @DisplayName("유효한 주문번호가 주어질 때 주문을 조회하면 성공한다")
    @Test
    void givenValidOrderNumber_whenFindOrderByOrderNumber_thenSuccess() {
        // Given
        String orderNumber = "TEST_ORDER_NUMBER";
        Member buyer = createMember(1L, "buyer", "buyer@test.com");
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .totalPrice(50000)
                .status(OrderStatus.ORDER_COMPLETE)
                .buyer(buyer)
                .destination("서울시 강남구")
                .build();

        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(order));

        // When
        Order result = orderService.findOrderByOrderNumber(orderNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo(orderNumber);
        verify(orderRepository, times(1)).findByOrderNumber(orderNumber);
    }

    @DisplayName("존재하지 않는 주문번호가 주어질 때 주문을 조회하면 예외가 발생한다")
    @Test
    void givenNonExistentOrderNumber_whenFindOrderByOrderNumber_thenThrowException() {
        // Given
        String orderNumber = "INVALID_ORDER_NUMBER";

        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.findOrderByOrderNumber(orderNumber))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.ORDER_NOT_FOUND);
    }

    @DisplayName("유효한 주문번호가 주어질 때 구매자 정보와 함께 조회하면 성공한다")
    @Test
    void givenValidOrderNumber_whenFindOrderByOrderNumberWithBuyer_thenSuccess() {
        // Given
        String orderNumber = "TEST_ORDER_NUMBER";
        Member buyer = createMember(1L, "buyer", "buyer@test.com");
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .totalPrice(50000)
                .status(OrderStatus.ORDER_COMPLETE)
                .buyer(buyer)
                .destination("서울시 강남구")
                .build();

        when(orderRepository.findByOrderNumberWithBuyer(orderNumber)).thenReturn(Optional.of(order));

        // When
        Order result = orderService.findOrderByOrderNumberWithBuyer(orderNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo(orderNumber);
        assertThat(result.getBuyer()).isNotNull();
        verify(orderRepository, times(1)).findByOrderNumberWithBuyer(orderNumber);
    }

    @DisplayName("유효한 주문번호가 주어질 때 상품 정보와 함께 조회하면 성공한다")
    @Test
    void givenValidOrderNumber_whenFindOrderByOrderNumberWithProductsForUpdate_thenSuccess() {
        // Given
        String orderNumber = "TEST_ORDER_NUMBER";
        Member buyer = createMember(1L, "buyer", "buyer@test.com");
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .totalPrice(50000)
                .status(OrderStatus.ORDER_COMPLETE)
                .buyer(buyer)
                .destination("서울시 강남구")
                .build();

        when(orderRepository.findByOrderNumberWithProductsForUpdate(orderNumber)).thenReturn(Optional.of(order));

        // When
        Order result = orderService.findOrderByOrderNumberWithProductsForUpdate(orderNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo(orderNumber);
        verify(orderRepository, times(1)).findByOrderNumberWithProductsForUpdate(orderNumber);
    }

    @DisplayName("유효한 주문번호가 주어질 때 모든 정보와 함께 조회하면 성공한다")
    @Test
    void givenValidOrderNumber_whenFindOrderByOrderNumberWithAll_thenSuccess() {
        // Given
        String orderNumber = "TEST_ORDER_NUMBER";
        Member buyer = createMember(1L, "buyer", "buyer@test.com");
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .totalPrice(50000)
                .status(OrderStatus.ORDER_COMPLETE)
                .buyer(buyer)
                .destination("서울시 강남구")
                .build();

        when(orderRepository.findByOrderNumberWithAll(orderNumber)).thenReturn(Optional.of(order));

        // When
        Order result = orderService.findOrderByOrderNumberWithAll(orderNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo(orderNumber);
        verify(orderRepository, times(1)).findByOrderNumberWithAll(orderNumber);
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
