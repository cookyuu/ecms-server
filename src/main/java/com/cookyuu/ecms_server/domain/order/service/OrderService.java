package com.cookyuu.ecms_server.domain.order.service;

import com.cookyuu.ecms_server.domain.cart.entity.Cart;
import com.cookyuu.ecms_server.domain.cart.entity.CartItem;
import com.cookyuu.ecms_server.domain.cart.service.CartService;
import com.cookyuu.ecms_server.domain.coupon.enums.CouponCode;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.domain.order.dto.*;
import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.domain.order.entity.OrderLine;
import com.cookyuu.ecms_server.domain.order.enums.OrderCode;
import com.cookyuu.ecms_server.domain.order.enums.OrderStatus;
import com.cookyuu.ecms_server.domain.order.mapper.CreateOrderLineMapper;
import com.cookyuu.ecms_server.domain.order.mapper.ReviseOrderLineMapper;
import com.cookyuu.ecms_server.domain.order.repository.OrderLineRepository;
import com.cookyuu.ecms_server.domain.order.repository.OrderRepository;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.domain.product.service.ProductService;
import com.cookyuu.ecms_server.common.enums.RedisKeyCode;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.security.service.AuthorizationService;
import com.cookyuu.ecms_server.common.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cookyuu.ecms_server.common.enums.ResultCode.ORDER_PROCESS_FAIL;
import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private static final int ORDER_NUMBER_EXPIRATION_SECONDS = 61;
    private static final int ORDER_NUMBER_RANDOM_SUFFIX_LENGTH = 5;
    private static final int RANDOM_DIGIT_BOUND = 10;

    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final MemberService memberService;
    private final CartService cartService;
    private final ProductService productService;
    private final RedisUtils redisUtils;
    private final AuthorizationService authorizationService;

    @Transactional
    public CreateOrderDto.Response createOrder(Long userId, CreateOrderDto.Request orderInfo) {
        long startTime = System.currentTimeMillis();

        Member buyer = memberService.findMemberById(userId);
        Cart cart = cartService.findCartByMemberIdWithCartItemsAndProducts(buyer.getId());
        List<Long> productIds = orderInfo.getOrderItemList().stream()
                .map(CreateOrderItemInfo::getProductId)
                .collect(Collectors.toList());
        List<Product> products = productService.findProductsByIdInWithLock(productIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        int totalPrice = 0;
        for (CreateOrderItemInfo orderItemInfo : orderInfo.getOrderItemList()) {
            Product product = productMap.get(orderItemInfo.getProductId());
            if (product == null) {
                log.atError()
                    .addKeyValue(EVENT, BUSINESS_ERROR)
                    .addKeyValue(USER_ID, userId)
                    .addKeyValue(PRODUCT_ID, orderItemInfo.getProductId())
                    .addKeyValue(ERROR_CODE, ResultCode.PRODUCT_NOT_FOUND.getCode())
                    .log("Order creation failed - product not found");
                throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
            }
            product.validateNotDeleted();
            int quantity = orderItemInfo.getQuantity();
            int price = orderItemInfo.getPrice();
            totalPrice += (quantity*price);

            compareQuantityAndStockQuantity(quantity, product.getStockQuantity());
            comparePriceAndCurrentPrice(price, product.getPrice(), product.getId());
            updateCartWithOrderItems(cart, product, orderItemInfo);
            orderItemInfo.addProduct(product);
        }

        String orderNumber = createOrderNumber(OrderCode.NORMAL_ORDER, CouponCode.NO_COUPON);
        while (redisUtils.getData(RedisKeyCode.ORDER_NUMBER.getSeparator()+orderNumber) != null) {
            orderNumber = createOrderNumber(OrderCode.NORMAL_ORDER, CouponCode.NO_COUPON);
        }

        try {
            String redisValueOfOrderNumber = "true";
            redisUtils.setDataExpire(RedisKeyCode.ORDER_NUMBER.getSeparator()+orderNumber, redisValueOfOrderNumber, ORDER_NUMBER_EXPIRATION_SECONDS);

            orderInfo.addTotalPrice(totalPrice);
            orderInfo.addBuyer(buyer);
            orderInfo.addOrderNumber(orderNumber);
            Order order = orderRepository.save(orderInfo.toEntity());

            orderLineRepository.saveAll(CreateOrderLineMapper.toEntityList(orderInfo.getOrderItemList(), order));

            for (CreateOrderItemInfo orderItemInfo : orderInfo.getOrderItemList()) {
                Product product = orderItemInfo.getProduct();
                int quantity = orderItemInfo.getQuantity();
                product.subQuantity(quantity);
            }

            log.atInfo()
                .addKeyValue(EVENT, ORDER_CREATED)
                .addKeyValue(USER_ID, userId)
                .addKeyValue(ORDER_ID, order.getId())
                .addKeyValue(ORDER_NUMBER, order.getOrderNumber())
                .addKeyValue(TOTAL_AMOUNT, order.getTotalPrice())
                .addKeyValue(ITEM_COUNT, order.getOrderLines().size())
                .addKeyValue(ORDER_STATUS, order.getStatus().name())
                .addKeyValue(DURATION_MS, System.currentTimeMillis() - startTime)
                .log("Order created successfully");

            return CreateOrderDto.Response.toDto(order);
        } catch (Exception e) {
            redisUtils.deleteData(RedisKeyCode.ORDER_NUMBER.getSeparator() + orderNumber);

            log.atError()
                .addKeyValue(EVENT, SYSTEM_ERROR)
                .addKeyValue(USER_ID, userId)
                .addKeyValue(ORDER_NUMBER, orderNumber)
                .addKeyValue(ERROR_MESSAGE, e.getMessage())
                .addKeyValue(DURATION_MS, System.currentTimeMillis() - startTime)
                .setCause(e)
                .log("Order creation failed - transaction error");
            throw e;
        }
    }

    @Transactional
    public ResultCode cancelOrder(UserDetails user, CancelOrderDto.Request cancelInfo) {
        long startTime = System.currentTimeMillis();
        Long userId = Long.parseLong(user.getUsername());

        Order order = findOrderByOrderNumberWithProductsForUpdate(cancelInfo.getOrderNumber());
        order.validateNotCanceled();
        authorizationService.validateResourceOwnership(userId, order.getBuyer().getId(), ResultCode.ORDER_BUYER_UNMATCHED);

        boolean isPossibleCancel = OrderStatus.isPossibleOrderCancel(order.getStatus());
        if (isPossibleCancel) {
            order.getOrderLines().forEach(orderLine ->
                orderLine.getProduct().addQuantity(orderLine.getQuantity()));
            order.cancel(cancelInfo.getCancelReason());

            log.atInfo()
                .addKeyValue(EVENT, ORDER_CANCELLED)
                .addKeyValue(USER_ID, userId)
                .addKeyValue(ORDER_ID, order.getId())
                .addKeyValue(ORDER_NUMBER, order.getOrderNumber())
                .addKeyValue(ORDER_STATUS, order.getStatus().name())
                .addKeyValue(CANCEL_REASON, cancelInfo.getCancelReason())
                .addKeyValue(DURATION_MS, System.currentTimeMillis() - startTime)
                .log("Order cancelled successfully");

            return ResultCode.ORDER_CANCEL_SUCCESS;
        } else {
            log.atWarn()
                .addKeyValue(EVENT, BUSINESS_ERROR)
                .addKeyValue(USER_ID, userId)
                .addKeyValue(ORDER_ID, order.getId())
                .addKeyValue(ORDER_NUMBER, order.getOrderNumber())
                .addKeyValue(ORDER_STATUS, order.getStatus().name())
                .addKeyValue(ERROR_CODE, ResultCode.ORDER_CANCEL_FAIL.getCode())
                .addKeyValue(ERROR_MESSAGE, "Cannot cancel order in current status")
                .log("Order cancellation failed - invalid status");

            throw new BusinessException(ResultCode.ORDER_CANCEL_FAIL, "주문 취소 요청을 할 수 없는 상태입니다. ");
        }
    }

    @Transactional
    @CacheEvict(
            value = "routes",
            key = "'order:number:' + #reviseOrderInfo.orderNumber"
    )
    public ResultCode reviseOrder(UserDetails user, ReviseOrderDto.Request reviseOrderInfo) {
        Order order = findOrderByOrderNumberWithProductsForUpdate(reviseOrderInfo.getOrderNumber());
        order.validateNotCanceled();
        boolean isPossibleRevise = OrderStatus.isPossibleOrderRevise(order.getStatus());
        if (!isPossibleRevise) {
            log.atWarn()
                .addKeyValue(EVENT, BUSINESS_ERROR)
                .addKeyValue(ORDER_ID, order.getId())
                .addKeyValue(ORDER_STATUS, order.getStatus().name())
                .addKeyValue(ERROR_CODE, ResultCode.ORDER_CANCEL_FAIL.getCode())
                .addKeyValue(ERROR_MESSAGE, "Cannot revise order in current status")
                .log("Order revision failed - invalid status");
            throw new BusinessException(ResultCode.ORDER_CANCEL_FAIL, "주문 취소 요청을 할 수 없는 상태입니다. ");
        }

        List<OrderLine> orderLines = order.getOrderLines();
        authorizationService.validateResourceOwnership(Long.parseLong(user.getUsername()), order.getBuyer().getId(), ResultCode.ORDER_BUYER_UNMATCHED);

        List<Long> oldProductIds = orderLines.stream()
                .map(orderLine -> orderLine.getProduct().getId())
                .collect(Collectors.toList());
        List<Product> oldProducts = productService.findProductsByIdInWithLock(oldProductIds);

        Map<Long, Product> oldProductMap = oldProducts.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        for (OrderLine orderLine : orderLines) {
            Product product = oldProductMap.get(orderLine.getProduct().getId());
            if (product != null) {
                product.addQuantity(orderLine.getQuantity());
                log.atDebug()
                    .addKeyValue(EVENT, ORDER_REVISED)
                    .addKeyValue(PRODUCT_ID, product.getId())
                    .addKeyValue(QUANTITY, orderLine.getQuantity())
                    .log("Restored product quantity for order revision");
            }
        }

        List<Long> newProductIds = reviseOrderInfo.getOrderItemList().stream()
                .map(ReviseOrderItemInfo::getProductId)
                .collect(Collectors.toList());
        List<Product> newProducts = productService.findProductsByIdInWithLock(newProductIds);

        Map<Long, Product> newProductMap = newProducts.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        int totalPrice = 0;
        for (ReviseOrderItemInfo orderItemInfo : reviseOrderInfo.getOrderItemList()) {
            Product product = newProductMap.get(orderItemInfo.getProductId());
            if (product == null) {
                throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
            }
            product.validateNotDeleted();
            int quantity = orderItemInfo.getQuantity();
            int price = orderItemInfo.getPrice();
            totalPrice += (quantity*price);
            log.atDebug()
                .addKeyValue(EVENT, ORDER_REVISED)
                .addKeyValue(PRODUCT_ID, product.getId())
                .addKeyValue(PRODUCT_PRICE, price)
                .log("Comparing product price for order revision");
            compareQuantityAndStockQuantity(quantity, product.getStockQuantity());
            comparePriceAndCurrentPrice(price, product.getPrice(), product.getId());
            orderItemInfo.addProduct(product);
        }
        orderLineRepository.deleteAll(orderLines);
        orderLineRepository.saveAll(ReviseOrderLineMapper.toEntityList(reviseOrderInfo.getOrderItemList(), order));
        order.reviseOrder(totalPrice);

        for (ReviseOrderItemInfo orderItemInfo : reviseOrderInfo.getOrderItemList()) {
            Product product = orderItemInfo.getProduct();
            int quantity = orderItemInfo.getQuantity();
            product.subQuantity(quantity);
        }

        log.atInfo()
            .addKeyValue(EVENT, ORDER_REVISED)
            .addKeyValue(USER_ID, Long.parseLong(user.getUsername()))
            .addKeyValue(ORDER_ID, order.getId())
            .addKeyValue(ORDER_NUMBER, order.getOrderNumber())
            .addKeyValue(TOTAL_AMOUNT, totalPrice)
            .addKeyValue(ITEM_COUNT, reviseOrderInfo.getOrderItemList().size())
            .log("Order revised successfully");

        return ResultCode.ORDER_REVISE_SUCCESS;
    }

    @Transactional(readOnly = true)
    public Page<SearchOrderDto.Response> searchOrderList(SearchOrderDto.Request searchInfo) {
        return orderRepository.searchPageOrderByCreatedAtDesc(searchInfo);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "routes",
            key = "'order:number:' + #orderNumber"
    )
    public OrderDetailDto getOrderDetailCacheable(UserDetails user , String orderNumber) {
        RoleType userRole = authorizationService.getUserRole(user);
        log.atDebug()
            .addKeyValue("operation", "getOrderDetail")
            .addKeyValue(USER_ROLE, userRole.name())
            .addKeyValue(ORDER_NUMBER, orderNumber)
            .log("Fetching order detail");

        OrderDetailDto orderDetailInfo = getOrderDetailBy(orderNumber);

        if (userRole == RoleType.USER) {
            authorizationService.validateOrderBuyerAccess(user, orderDetailInfo.getOrderInfo().getBuyerId());
        } else if (userRole == RoleType.SELLER) {
            List<Long> sellerIds = orderDetailInfo.getOrderLines().stream()
                .map(orderLineInfo -> orderLineInfo.getSellerId())
                .collect(Collectors.toList());
            authorizationService.validateOrderSellerAccess(user, sellerIds);
        }

        return orderDetailInfo;
    }

    private OrderDetailDto getOrderDetailBy(String orderNumber) {
        return orderRepository.getOrderDetail(orderNumber);
    }

    private void compareQuantityAndStockQuantity(int quantity, Integer stockQuantity) {
        if (stockQuantity == 0) {
            log.atError()
                .addKeyValue(EVENT, PRODUCT_OUT_OF_STOCK)
                .addKeyValue(STOCK_QUANTITY, stockQuantity)
                .addKeyValue(QUANTITY, quantity)
                .addKeyValue(ERROR_CODE, ResultCode.PRODUCT_SOLD_OUT.getCode())
                .log("Product sold out - zero stock");
            throw new BusinessException(ResultCode.PRODUCT_SOLD_OUT, "주문하신 상품의 재고 수량이 없습니다.");
        }
        if (quantity > stockQuantity) {
            log.atError()
                .addKeyValue(EVENT, PRODUCT_OUT_OF_STOCK)
                .addKeyValue(STOCK_QUANTITY, stockQuantity)
                .addKeyValue(QUANTITY, quantity)
                .addKeyValue(ERROR_CODE, ResultCode.PRODUCT_SOLD_OUT.getCode())
                .log("Insufficient stock - order quantity exceeds available stock");
            throw new BusinessException(ResultCode.PRODUCT_SOLD_OUT, "주문하신 상품의 재고 수량이 부족합니다. 재고 수량 : " + stockQuantity);
        }
        log.atDebug()
            .addKeyValue("operation", "compareStockQuantity")
            .addKeyValue(STOCK_QUANTITY, stockQuantity)
            .addKeyValue(QUANTITY, quantity)
            .log("Stock quantity validation passed");
    }
    private void updateCartWithOrderItems(Cart cart, Product product, CreateOrderItemInfo orderItemInfo) {
        CartItem cartItem = findCartItemByCartAndProduct(cart, product);
        if (cartItem == null) {
            return ;
        }
        log.atDebug()
            .addKeyValue("operation", "updateCart")
            .addKeyValue(CART_ID, cart.getId())
            .addKeyValue(PRODUCT_ID, product.getId())
            .log("Updating cart with ordered items");
        if (cartItem.getQuantity() <= orderItemInfo.getQuantity()) {
            cartService.deleteCartItem(cart, product);
        } else {
            cartItem.updateQuantity(cartItem.getQuantity() - orderItemInfo.getQuantity());
        }
    }

    private void comparePriceAndCurrentPrice(int price, Integer currentPrice, Long productId) {
        if (currentPrice == null) {
            log.atError()
                .addKeyValue(EVENT, VALIDATION_ERROR)
                .addKeyValue(PRODUCT_ID, productId)
                .addKeyValue(ERROR_CODE, ORDER_PROCESS_FAIL.getCode())
                .addKeyValue(ERROR_MESSAGE, "Product price not set")
                .log("Product price validation failed - price not set");
            throw new BusinessException(ORDER_PROCESS_FAIL, "가격이 아직 책정되지 않은 상품이 있습니다.");
        }
        if (price != currentPrice) {
            log.atError()
                .addKeyValue(EVENT, VALIDATION_ERROR)
                .addKeyValue(PRODUCT_ID, productId)
                .addKeyValue("order_price", price)
                .addKeyValue("current_price", currentPrice)
                .addKeyValue(ERROR_CODE, ORDER_PROCESS_FAIL.getCode())
                .log("Price validation failed - order price and current price mismatch");
            throw new BusinessException(ORDER_PROCESS_FAIL, "상품의 현재 가격과 주문 가격이 일치하지 않습니다.");
        }
        log.atDebug()
            .addKeyValue("operation", "comparePriceAndCurrentPrice")
            .addKeyValue(PRODUCT_ID, productId)
            .addKeyValue(PRODUCT_PRICE, currentPrice)
            .log("Price validation passed");
    }

    private CartItem findCartItemByCartAndProduct(Cart cart, Product product) {
        return cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getProduct().equals(product))
                .findFirst()
                .orElse(null);
    }

    private String createOrderNumber(OrderCode order, CouponCode coopon) {
        StringBuilder sb = new StringBuilder();
        String formatDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
        sb.append(order.getCode()).append(formatDate).append(coopon.getCode());
        for (int i = 0; i < ORDER_NUMBER_RANDOM_SUFFIX_LENGTH; i++) {
            int random = (int) (Math.random() * RANDOM_DIGIT_BOUND);
            sb.append(random);
        }
        return sb.toString();
    }

    public Order findOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber).orElseThrow(() -> new BusinessException(ResultCode.ORDER_NOT_FOUND));
    }

    public Order findOrderByOrderNumberWithBuyer(String orderNumber) {
        return orderRepository.findByOrderNumberWithBuyer(orderNumber)
                .orElseThrow(() -> new BusinessException(ResultCode.ORDER_NOT_FOUND));
    }

    public Order findOrderByOrderNumberWithProductsForUpdate(String orderNumber) {
        return orderRepository.findByOrderNumberWithProductsForUpdate(orderNumber)
                .orElseThrow(() -> new BusinessException(ResultCode.ORDER_NOT_FOUND));
    }

    public Order findOrderByOrderNumberWithAll(String orderNumber) {
        return orderRepository.findByOrderNumberWithAll(orderNumber)
                .orElseThrow(() -> new BusinessException(ResultCode.ORDER_NOT_FOUND));
    }
}
