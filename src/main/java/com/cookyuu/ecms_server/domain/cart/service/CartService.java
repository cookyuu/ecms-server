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
import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.domain.ECMSCartException;
import com.cookyuu.ecms_server.global.exception.domain.ECMSCartItemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberService memberService;
    private final ProductService productService;

    public void makeCart(Member member) {
        Cart cart = Cart.builder().member(member).build();
        cartRepository.save(cart);
        log.info("[MakeCart] Make member's cart OK!, Member id : {}", member.getId());
    }

    @Transactional
    public void updateCartItem(UserDetails user, UpdateCartItemDto.Request cartItemInfo) {
        Member member = memberService.findMemberById(Long.parseLong(user.getUsername()));
        Product product = productService.findProductById(cartItemInfo.getProductId());
        product.isDeleted();
        Cart cart = cartRepository.findByMemberId(member.getId()).orElseThrow(ECMSCartException::new);
        if (cartItemInfo.getQuantity() < 1) {
            log.error("[UpdateCartItem] CartItem quantity is too less, Quantity : {}", cartItemInfo.getQuantity());
            throw new ECMSCartItemException(ResultCode.BAD_REQUEST, "카트에 담길 상품의 수량은 1이상 이여야합니다.");
        }
        if (cartItemRepository.existsByCartAndProduct(cart, product)) {
            CartItem cartItem = findCartItemByCartAndProduct(cart, product);
            cartItem.updateQuantity(cartItemInfo.getQuantity());
        } else {
            CartItem cartItem = UpdateCartItemMapper.toEntity(cartItemInfo, product, cart);
            cartItemRepository.save(cartItem);
        }
        log.info("[UpdateCartItem] Update cart item product quantity OK!, CartId : {}, productId : {}", cart.getId(), product.getId());
    }

    @Transactional
    public void deleteCartItem(UserDetails user, DeleteCartItemDto.Request cartItemInfo) {
        Member member = memberService.findMemberById(Long.parseLong(user.getUsername()));
        Product product = productService.findProductById(cartItemInfo.getProductId());
        Cart cart = cartRepository.findByMemberId(member.getId()).orElseThrow(ECMSCartException::new);
        CartItem cartItem = findCartItemByCartAndProduct(cart, product);
        cartItemRepository.delete(cartItem);
        log.info("[DeleteCartItem] Delete cart item OK!, CartId : {}, ProductId : {}", cart.getId(), product.getId());
    }

    public void deleteCartItem(Cart cart, Product product) {
        CartItem cartItem = findCartItemByCartAndProduct(cart, product);
        cartItemRepository.delete(cartItem);
    }

    private CartItem findCartItemByCartAndProduct(Cart cart, Product product) {
        return cartItemRepository.findByCartAndProduct(cart, product).orElseThrow(ECMSCartItemException::new);
    }

    public Cart findCartByMemberId(Long id) {
        return cartRepository.findByMemberId(id).orElseThrow(ECMSCartException::new);
    }
}
