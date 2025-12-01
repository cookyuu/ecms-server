package com.cookyuu.ecms_server.domain.cart.service;

import com.cookyuu.ecms_server.domain.cart.dto.DeleteCartItemDto;
import com.cookyuu.ecms_server.domain.cart.dto.UpdateCartItemDto;
import com.cookyuu.ecms_server.domain.cart.entity.Cart;
import com.cookyuu.ecms_server.domain.cart.entity.CartItem;
import com.cookyuu.ecms_server.domain.cart.mapper.UpdateCartItemMapper;
import com.cookyuu.ecms_server.domain.cart.repository.CartItemRepository;
import com.cookyuu.ecms_server.domain.cart.repository.CartRepository;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.domain.product.service.ProductService;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberService memberService;
    private final ProductService productService;

    @Transactional
    public void makeCart(Member member) {
        Cart cart = Cart.builder().member(member).build();
        cartRepository.save(cart);
        log.atInfo()
                .addKeyValue(EVENT, CART_CREATED)
                .addKeyValue(CART_ID, cart.getId())
                .addKeyValue(MEMBER_ID, member.getId())
                .log("Cart created successfully");
    }

    @Transactional
    public void updateCartItem(UserDetails user, UpdateCartItemDto.Request cartItemInfo) {
        Member member = memberService.findMemberById(Long.parseLong(user.getUsername()));
        Product product = productService.findProductById(cartItemInfo.getProductId());
        product.isDeleted();
        Cart cart = cartRepository.findByMemberId(member.getId()).orElseThrow(() -> new BusinessException(ResultCode.CART_NOT_FOUND));
        if (cartItemInfo.getQuantity() < 1) {
            log.atError()
                    .addKeyValue(EVENT, VALIDATION_ERROR)
                    .addKeyValue(QUANTITY, cartItemInfo.getQuantity())
                    .addKeyValue(CART_ID, cart.getId())
                    .addKeyValue(PRODUCT_ID, cartItemInfo.getProductId())
                    .log("Cart item quantity must be at least 1");
            throw new BusinessException(ResultCode.BAD_REQUEST, "카트에 담길 상품의 수량은 1이상 이여야합니다.");
        }
        if (cartItemRepository.existsByCartAndProduct(cart, product)) {
            CartItem cartItem = findCartItemByCartAndProduct(cart, product);
            cartItem.updateQuantity(cartItemInfo.getQuantity());
            log.atInfo()
                    .addKeyValue(EVENT, CART_ITEM_UPDATED)
                    .addKeyValue(CART_ID, cart.getId())
                    .addKeyValue(CART_ITEM_ID, cartItem.getId())
                    .addKeyValue(PRODUCT_ID, product.getId())
                    .addKeyValue(QUANTITY, cartItemInfo.getQuantity())
                    .log("Cart item quantity updated");
        } else {
            CartItem cartItem = UpdateCartItemMapper.toEntity(cartItemInfo, product, cart);
            cartItemRepository.save(cartItem);
            log.atInfo()
                    .addKeyValue(EVENT, CART_ITEM_ADDED)
                    .addKeyValue(CART_ID, cart.getId())
                    .addKeyValue(CART_ITEM_ID, cartItem.getId())
                    .addKeyValue(PRODUCT_ID, product.getId())
                    .addKeyValue(QUANTITY, cartItemInfo.getQuantity())
                    .log("Cart item added successfully");
        }
    }

    @Transactional
    public void deleteCartItem(UserDetails user, DeleteCartItemDto.Request cartItemInfo) {
        Member member = memberService.findMemberById(Long.parseLong(user.getUsername()));
        Product product = productService.findProductById(cartItemInfo.getProductId());
        Cart cart = cartRepository.findByMemberId(member.getId()).orElseThrow(() -> new BusinessException(ResultCode.CART_NOT_FOUND));
        CartItem cartItem = findCartItemByCartAndProduct(cart, product);
        cartItemRepository.delete(cartItem);
        log.atInfo()
                .addKeyValue(EVENT, CART_ITEM_REMOVED)
                .addKeyValue(CART_ID, cart.getId())
                .addKeyValue(CART_ITEM_ID, cartItem.getId())
                .addKeyValue(PRODUCT_ID, product.getId())
                .log("Cart item removed successfully");
    }

    @Transactional
    public void deleteCartItem(Cart cart, Product product) {
        CartItem cartItem = findCartItemByCartAndProduct(cart, product);
        cartItemRepository.delete(cartItem);
    }

    private CartItem findCartItemByCartAndProduct(Cart cart, Product product) {
        return cartItemRepository.findByCartAndProduct(cart, product).orElseThrow(() -> new BusinessException(ResultCode.CARTITEM_NOT_FOUND));
    }

    public Cart findCartByMemberId(Long id) {
        return cartRepository.findByMemberId(id).orElseThrow(() -> new BusinessException(ResultCode.CART_NOT_FOUND));
    }

    public Cart findCartByMemberIdWithCartItemsAndProducts(Long id) {
        return cartRepository.findByMemberIdWithCartItemsAndProducts(id)
                .orElseThrow(() -> new BusinessException(ResultCode.CART_NOT_FOUND));
    }
}
