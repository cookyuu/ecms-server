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
import com.cookyuu.ecms_server.common.generator.BusinessNumberGenerator;
import com.cookyuu.ecms_server.common.security.service.AuthorizationService;
import com.cookyuu.ecms_server.common.utils.RedisUtils;
import com.cookyuu.ecms_server.domain.order.logging.OrderLogHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final int ORDER_NUMBER_EXPIRATION_SECONDS = 61;

    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final MemberService memberService;
    private final CartService cartService;
    private final ProductService productService;
    private final RedisUtils redisUtils;
    private final AuthorizationService authorizationService;
    private final BusinessNumberGenerator businessNumberGenerator;
    private final OrderValidator orderValidator;
    private final OrderStockManager orderStockManager;
    private final OrderLogHelper orderLogHelper;

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
                orderLogHelper.logProductNotFoundError(userId, orderItemInfo.getProductId());
                throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
            }
            product.validateNotDeleted();
            int quantity = orderItemInfo.getQuantity();
            int price = orderItemInfo.getPrice();
            totalPrice += (quantity*price);

            orderValidator.validateStockQuantity(quantity, product.getStockQuantity(), product.getId());
            orderValidator.validateProductPrice(price, product.getPrice(), product.getId());
            updateCartWithOrderItems(cart, product, orderItemInfo);
            orderItemInfo.addProduct(product);
        }

        String orderNumber = businessNumberGenerator.generateOrderNumber(OrderCode.NORMAL_ORDER, CouponCode.NO_COUPON);
        while (redisUtils.getData(RedisKeyCode.ORDER_NUMBER.getSeparator()+orderNumber) != null) {
            orderNumber = businessNumberGenerator.generateOrderNumber(OrderCode.NORMAL_ORDER, CouponCode.NO_COUPON);
        }

        try {
            String redisValueOfOrderNumber = "true";
            redisUtils.setDataExpire(RedisKeyCode.ORDER_NUMBER.getSeparator()+orderNumber, redisValueOfOrderNumber, ORDER_NUMBER_EXPIRATION_SECONDS);

            orderInfo.addTotalPrice(totalPrice);
            orderInfo.addBuyer(buyer);
            orderInfo.addOrderNumber(orderNumber);
            Order order = orderRepository.save(orderInfo.toEntity());

            orderLineRepository.saveAll(CreateOrderLineMapper.toEntityList(orderInfo.getOrderItemList(), order));

            orderStockManager.decreaseStockForOrder(orderInfo.getOrderItemList());

            orderLogHelper.logOrderCreated(userId, order.getId(), order.getOrderNumber(),
                order.getTotalPrice(), order.getOrderLines().size(), order.getStatus().name(),
                System.currentTimeMillis() - startTime);

            return CreateOrderDto.Response.toDto(order);
        } catch (Exception e) {
            redisUtils.deleteData(RedisKeyCode.ORDER_NUMBER.getSeparator() + orderNumber);

            orderLogHelper.logOrderCreationFailed(userId, orderNumber, e.getMessage(),
                System.currentTimeMillis() - startTime, e);
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
            orderStockManager.restoreStockForCancel(order.getOrderLines());
            order.cancel(cancelInfo.getCancelReason());

            orderLogHelper.logOrderCancelled(userId, order.getId(), order.getOrderNumber(),
                order.getStatus().name(), cancelInfo.getCancelReason(),
                System.currentTimeMillis() - startTime);

            return ResultCode.ORDER_CANCEL_SUCCESS;
        } else {
            orderLogHelper.logOrderCancellationFailed(userId, order.getId(), order.getOrderNumber(),
                order.getStatus().name(), ResultCode.ORDER_CANCEL_FAIL);

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
            orderLogHelper.logOrderRevisionFailed(order.getId(), order.getStatus().name(),
                ResultCode.ORDER_CANCEL_FAIL);
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

        orderStockManager.restoreStockForRevision(orderLines);

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
            orderLogHelper.logPriceComparison(product.getId(), price);
            orderValidator.validateStockQuantity(quantity, product.getStockQuantity(), product.getId());
            orderValidator.validateProductPrice(price, product.getPrice(), product.getId());
            orderItemInfo.addProduct(product);
        }
        orderLineRepository.deleteAll(orderLines);
        orderLineRepository.saveAll(ReviseOrderLineMapper.toEntityList(reviseOrderInfo.getOrderItemList(), order));
        order.reviseOrder(totalPrice);

        orderStockManager.decreaseStockForRevision(reviseOrderInfo.getOrderItemList());

        orderLogHelper.logOrderRevised(Long.parseLong(user.getUsername()), order.getId(),
            order.getOrderNumber(), totalPrice, reviseOrderInfo.getOrderItemList().size());

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
        orderLogHelper.logOrderDetailFetch(userRole.name(), orderNumber);

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

    private void updateCartWithOrderItems(Cart cart, Product product, CreateOrderItemInfo orderItemInfo) {
        CartItem cartItem = findCartItemByCartAndProduct(cart, product);
        if (cartItem == null) {
            return ;
        }
        orderLogHelper.logCartUpdate(cart.getId(), product.getId());
        if (cartItem.getQuantity() <= orderItemInfo.getQuantity()) {
            cartService.deleteCartItem(cart, product);
        } else {
            cartItem.updateQuantity(cartItem.getQuantity() - orderItemInfo.getQuantity());
        }
    }

    private CartItem findCartItemByCartAndProduct(Cart cart, Product product) {
        return cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getProduct().equals(product))
                .findFirst()
                .orElse(null);
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
