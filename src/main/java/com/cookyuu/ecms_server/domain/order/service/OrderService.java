package com.cookyuu.ecms_server.domain.order.service;

import com.cookyuu.ecms_server.domain.cart.entity.Cart;
import com.cookyuu.ecms_server.domain.cart.entity.CartItem;
import com.cookyuu.ecms_server.domain.cart.service.CartService;
import com.cookyuu.ecms_server.domain.coupon.entity.CouponCode;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.entity.RoleType;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.domain.order.dto.*;
import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.domain.order.entity.OrderLine;
import com.cookyuu.ecms_server.domain.order.entity.OrderCode;
import com.cookyuu.ecms_server.domain.order.entity.OrderStatus;
import com.cookyuu.ecms_server.domain.order.mapper.CreateOrderLineMapper;
import com.cookyuu.ecms_server.domain.order.mapper.ReviseOrderLineMapper;
import com.cookyuu.ecms_server.domain.order.repository.OrderLineRepository;
import com.cookyuu.ecms_server.domain.order.repository.OrderRepository;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.domain.product.service.ProductService;
import com.cookyuu.ecms_server.global.code.RedisKeyCode;
import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.domain.ECMSOrderException;
import com.cookyuu.ecms_server.global.utils.JwtUtils;
import com.cookyuu.ecms_server.global.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.cookyuu.ecms_server.global.code.ResultCode.ORDER_PROCESS_FAIL;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final MemberService memberService;
    private final CartService cartService;
    private final ProductService productService;
    private final RedisUtils redisUtils;

    @Transactional
    public CreateOrderDto.Response createOrder(Long userId, CreateOrderDto.Request orderInfo) {
        Member buyer = memberService.findMemberById(userId);
        Cart cart = cartService.findCartByMemberId(buyer.getId());
        int totalPrice = 0;
        for (CreateOrderItemInfo orderItemInfo : orderInfo.getOrderItemList()) {
            Product product = productService.findProductById(orderItemInfo.getProductId());
            product.isDeleted();
            int quantity = orderItemInfo.getQuantity();
            int price = orderItemInfo.getPrice();
            totalPrice += (quantity*price);
            log.info("[Order::CreateOrder] Compare product price, productId : {}", product.getId());
            compareQuantityAndStockQuantity(quantity, product.getStockQuantity());
            comparePriceAndCurrentPrice(price, product.getPrice(), product.getId());
            updateCartWithOrderItems(cart, product, orderItemInfo);
            orderItemInfo.addProduct(product);
            product.subQuantity(quantity);
        }
        String orderNumber = createOrderNumber(OrderCode.NORMAL_ORDER, CouponCode.NO_COUPON);
        log.info("[Order::CreateOrder] Create order number, Order Number : {}", orderNumber);
        while (redisUtils.getData(RedisKeyCode.ORDER_NUMBER.getSeparator()+orderNumber) != null) {
            orderNumber = createOrderNumber(OrderCode.NORMAL_ORDER, CouponCode.NO_COUPON);
            log.debug("[Order::CreateOrder] Created order number is duplicated, Order Number : {}", orderNumber);
        }
        try {
            int orderNumberExp = 61;
            String redisValueOfOrderNumber = "true";
            redisUtils.setDataExpire(RedisKeyCode.ORDER_NUMBER.getSeparator()+orderNumber, redisValueOfOrderNumber, orderNumberExp);

            orderInfo.addTotalPrice(totalPrice);
            orderInfo.addBuyer(buyer);
            orderInfo.addOrderNumber(orderNumber);
            Order order = orderRepository.save(orderInfo.toEntity());
            log.debug("[Order::CreateOrder] Save order info.");
            orderLineRepository.saveAll(CreateOrderLineMapper.toEntityList(orderInfo.getOrderItemList(), order));
            return CreateOrderDto.Response.toDto(order);
        } catch (Exception e) {
            redisUtils.deleteData(RedisKeyCode.ORDER_NUMBER.getSeparator() + orderNumber);
            log.error("[Order::Error] Transaction failed, rollback Redis key. Order Number : {}", orderNumber);
            throw e;
        }
    }

    @Transactional
    public ResultCode cancelOrder(UserDetails user, CancelOrderDto.Request cancelInfo) {
        Order order = findOrderByOrderNumber(cancelInfo.getOrderNumber());
        order.isCanceled();
        checkBuyerOfOrder(Long.parseLong(user.getUsername()), order.getBuyer().getId());
        boolean isPossibleCancel = OrderStatus.isPossibleOrderCancel(order.getStatus());
        if (isPossibleCancel) {
            order.getOrderLines().forEach(orderLine -> orderLine.getProduct().addQuantity(orderLine.getQuantity()));
            order.cancel(cancelInfo.getCancelReason());
            log.info("[Order::Cancel] Cancel request of Order OK!, orderNumber : {}", order.getOrderNumber());
        } else {
            log.info("[Order::Cancel] Can not cancel order, order status is {}", order.getStatus());
            throw new ECMSOrderException(ResultCode.ORDER_CANCEL_FAIL, "주문 취소 요청을 할 수 없는 상태입니다. ");
        }
        log.debug("[Order::Cancel] Order cancel request is OK!");
        return ResultCode.ORDER_CANCEL_SUCCESS;
    }

    @Transactional
    public ResultCode reviseOrder(UserDetails user, ReviseOrderDto.Request reviseOrderInfo) {
        Order order = findOrderByOrderNumber(reviseOrderInfo.getOrderNumber());
        order.isCanceled();
        boolean isPossibleRevise = OrderStatus.isPossibleOrderRevise(order.getStatus());
        if (!isPossibleRevise) {
            log.info("[Order::Revise] Can not revise order, order status is {}", order.getStatus());
            throw new ECMSOrderException(ResultCode.ORDER_CANCEL_FAIL, "주문 취소 요청을 할 수 없는 상태입니다. ");
        }

        List<OrderLine> orderLines = order.getOrderLines();
        checkBuyerOfOrder(Long.parseLong(user.getUsername()), order.getBuyer().getId());
        orderLines.forEach(orderLine -> orderLine.getProduct().addQuantity(orderLine.getQuantity()));
        orderLineRepository.deleteAll(orderLines);

        int totalPrice = 0;
        for (ReviseOrderItemInfo orderItemInfo : reviseOrderInfo.getOrderItemList()) {
            Product product = productService.findProductById(orderItemInfo.getProductId());
            int quantity = orderItemInfo.getQuantity();
            int price = orderItemInfo.getPrice();
            totalPrice += (quantity*price);
            log.info("[Order::Revise] Compare product price, productId : {}", product.getId());
            compareQuantityAndStockQuantity(quantity, product.getStockQuantity());
            comparePriceAndCurrentPrice(price, product.getPrice(), product.getId());
            orderItemInfo.addProduct(product);
        }
        orderLineRepository.saveAll(ReviseOrderLineMapper.toEntityList(reviseOrderInfo.getOrderItemList(), order));
        order.reviseOrder(totalPrice);
        log.info("[Order::Revise] Revise order info is OK!, orderNumber : {}", order.getOrderNumber());

        return ResultCode.ORDER_REVISE_SUCCESS;
    }

    @Transactional(readOnly = true)
    public Page<SearchOrderDto.Response> searchOrderList(SearchOrderDto.Request searchInfo) {
        return orderRepository.searchPageOrderByCreatedAtDesc(searchInfo);
    }

    @Transactional(readOnly = true)
    public OrderDetailDto getOrderDetail(UserDetails user, String orderNumber) {
        String jwtRole = JwtUtils.getRoleFromUserDetails(user);
        log.debug("[Order::getDetail] ROLE : {}", jwtRole);
        OrderDetailDto orderDetailInfo = getOrderDetailBy(orderNumber);
        if (jwtRole.equals("ROLE_"+RoleType.USER.name())) {
            Long buyerId = orderDetailInfo.getOrderInfo().getBuyerId();
            checkBuyerOfOrder(Long.parseLong(user.getUsername()), buyerId);
        } else if (jwtRole.equals("ROLE_"+RoleType.SELLER.name())) {
            boolean isSellerOfOrder = orderDetailInfo.getOrderLines().stream().anyMatch(orderLineInfo -> checkSellerOfOrder(Long.parseLong(user.getUsername()), orderLineInfo.getSellerId()));
            if (!isSellerOfOrder) {
                throw new ECMSOrderException(ResultCode.ORDER_SELLER_UNMATCHED);
            }
        }
        return orderDetailInfo;
    }

    private OrderDetailDto getOrderDetailBy(String orderNumber) {
        return orderRepository.getOrderDetail(orderNumber);
    }

    private void checkBuyerOfOrder(Long reqUserId, Long buyerId) {
        log.debug("[Order::BuyerMatch] Unmatched Order's buyer Info and request User Info, buyerId : {}, reqUserId : {}", buyerId, reqUserId);
        if (!buyerId.equals(reqUserId)) {
            throw new ECMSOrderException(ResultCode.ORDER_BUYER_UNMATCHED);
        }
        log.info("[Order::BuyerMatch] Match buyer id and request user id OK!");
    }

    private boolean checkSellerOfOrder(long reqUserId, Long sellerId) {
        log.debug("[Order::SellerMatch] Unmatched Order's seller Info and request User Info, sellerId : {}, reqUserId : {}", sellerId, reqUserId);
        return sellerId.equals(reqUserId);
    }

    private void compareQuantityAndStockQuantity(int quantity, Integer stockQuantity) {
        if (stockQuantity == 0) {
            log.error("[Order::Error] This product stock quantity is zero, Product Sold out!");
            throw new ECMSOrderException(ResultCode.PRODUCT_SOLD_OUT, "주문하신 상품의 재고 수량이 없습니다.");
        }
        if (quantity > stockQuantity) {
            log.error("[Order::Error] This product stock quantity is less than order quantity, stockQuantity : {}, orderQuantity : {}", stockQuantity, quantity);
            throw new ECMSOrderException(ResultCode.PRODUCT_SOLD_OUT, "주문하신 상품의 재고 수량이 부족합니다. 재고 수량 : " + stockQuantity);
        }
        log.info("[Order::CompareStockQuantity] This product ");
    }
    private void updateCartWithOrderItems(Cart cart, Product product, CreateOrderItemInfo orderItemInfo) {
        CartItem cartItem = findCartItemByCartAndProduct(cart, product);
        log.debug("[Order::UpdateCart]");
        if (cartItem == null) {
            return ;
        }
        if (cartItem.getQuantity() <= orderItemInfo.getQuantity()) {
            cartService.deleteCartItem(cart, product);
        } else {
            cartItem.updateQuantity(cartItem.getQuantity() - orderItemInfo.getQuantity());
        }
    }

    private void comparePriceAndCurrentPrice(int price, Integer currentPrice, Long productId) {
        if (currentPrice == null) {
            log.error("[Order::Error] Product price has not been set yet, productId : {}", productId);
            throw new ECMSOrderException(ORDER_PROCESS_FAIL, "가격이 아직 책정되지 않은 상품이 있습니다.");
        }
        if (price != currentPrice) {
            log.error("[Order::Error] Compare product order price and current price Fail!, productId : {}, orderPrice : {}, currentPrice : {}", productId, price, currentPrice);
            throw new ECMSOrderException(ORDER_PROCESS_FAIL, "상품의 현재 가격과 주문 가격이 일치하지 않습니다.");
        }
        log.info("[CompareProductPriceForOrder] Compare product order price and current price OK!");
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
        for (int i = 0; i < 5; i++) {
            int random = (int) (Math.random() * 10);
            sb.append(random);
        }
        return sb.toString();
    }

    public Order findOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber).orElseThrow(ECMSOrderException::new);
    }
}
