package com.cookyuu.ecms_server.domain.cart.service;

import com.cookyuu.ecms_server.domain.cart.dto.DeleteCartItemDto;
import com.cookyuu.ecms_server.domain.cart.dto.UpdateCartItemDto;
import com.cookyuu.ecms_server.domain.cart.entity.Cart;
import com.cookyuu.ecms_server.domain.cart.entity.CartItem;
import com.cookyuu.ecms_server.domain.cart.logging.CartLogHelper;
import com.cookyuu.ecms_server.domain.cart.mapper.UpdateCartItemMapper;
import com.cookyuu.ecms_server.domain.cart.repository.CartItemRepository;
import com.cookyuu.ecms_server.domain.cart.repository.CartRepository;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.domain.product.service.ProductService;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberService memberService;
    private final ProductService productService;
    private final CartLogHelper cartLogHelper;
    private final UserUtils userUtils;

    @Transactional
    public void makeCart(Member member) {
        Cart cart = Cart.builder().member(member).build();
        cartRepository.save(cart);
        cartLogHelper.logCartCreated(cart.getId(), member.getId());
    }

    @Transactional
    public void updateCartItem(UserDetails user, UpdateCartItemDto.Request cartItemInfo) {
        Member member = memberService.findMemberById(userUtils.getUserId(user));
        Product product = productService.findProductById(cartItemInfo.getProductId());
        product.validateNotDeleted();
        Cart cart = cartRepository.findByMemberId(member.getId()).orElseThrow(() -> new BusinessException(ResultCode.CART_NOT_FOUND));
        if (cartItemInfo.getQuantity() < 1) {
            cartLogHelper.logCartItemQuantityValidationFailed(cart.getId(), cartItemInfo.getProductId(),
                cartItemInfo.getQuantity());
            throw new BusinessException(ResultCode.BAD_REQUEST, "카트에 담길 상품의 수량은 1이상 이여야합니다.");
        }
        if (cartItemRepository.existsByCartAndProduct(cart, product)) {
            CartItem cartItem = findCartItemByCartAndProduct(cart, product);
            cartItem.updateQuantity(cartItemInfo.getQuantity());
            cartLogHelper.logCartItemUpdated(cart.getId(), cartItem.getId(), product.getId(),
                cartItemInfo.getQuantity());
        } else {
            CartItem cartItem = UpdateCartItemMapper.toEntity(cartItemInfo, product, cart);
            cartItemRepository.save(cartItem);
            cartLogHelper.logCartItemAdded(cart.getId(), cartItem.getId(), product.getId(),
                cartItemInfo.getQuantity());
        }
    }

    @Transactional
    public void deleteCartItem(UserDetails user, DeleteCartItemDto.Request cartItemInfo) {
        Member member = memberService.findMemberById(userUtils.getUserId(user));
        Product product = productService.findProductById(cartItemInfo.getProductId());
        Cart cart = cartRepository.findByMemberId(member.getId()).orElseThrow(() -> new BusinessException(ResultCode.CART_NOT_FOUND));
        CartItem cartItem = findCartItemByCartAndProduct(cart, product);
        cartItemRepository.delete(cartItem);
        cartLogHelper.logCartItemRemoved(cart.getId(), cartItem.getId(), product.getId());
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
