package com.cookyuu.ecms_server.domain.cart.service;

import com.cookyuu.ecms_server.domain.cart.dto.AddCartItemDto;
import com.cookyuu.ecms_server.domain.cart.entity.Cart;
import com.cookyuu.ecms_server.domain.cart.entity.CartItem;
import com.cookyuu.ecms_server.domain.cart.mapper.AddCartItemMapper;
import com.cookyuu.ecms_server.domain.cart.repository.CartItemRepository;
import com.cookyuu.ecms_server.domain.cart.repository.CartRepository;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.domain.product.service.ProductService;
import com.cookyuu.ecms_server.global.exception.domain.ECMSCartException;
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
    public void addCartItem(UserDetails user, AddCartItemDto.Request cartItemInfo) {
        Member member = memberService.findMemberById(Long.parseLong(user.getUsername()));
        Product product = productService.findProductById(cartItemInfo.getProductId());
        Cart cart = cartRepository.findByMemberId(member.getId()).orElseThrow(ECMSCartException::new);
        if (cartItemRepository.existsByCartAndProduct(cart, product)) {
            CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product);
            cartItem.addQuantity(cartItemInfo.getQuantity());
        } else {
            CartItem cartItem = AddCartItemMapper.toEntity(cartItemInfo, product, cart);
            cartItemRepository.save(cartItem);
        }
    }
}
