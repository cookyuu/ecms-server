package com.cookyuu.ecms_server.domain.product.service;

import com.cookyuu.ecms_server.domain.product.dto.FindProductDetailDto;
import com.cookyuu.ecms_server.domain.product.dto.RegisterProductDto;
import com.cookyuu.ecms_server.domain.product.dto.SearchProductDto;
import com.cookyuu.ecms_server.domain.product.dto.UpdateProductDto;
import com.cookyuu.ecms_server.domain.product.entity.Category;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.domain.product.repository.ProductRepository;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import com.cookyuu.ecms_server.domain.seller.service.SellerService;
import com.cookyuu.ecms_server.global.code.CookieCode;
import com.cookyuu.ecms_server.global.code.RedisKeyCode;
import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.domain.ECMSProductException;
import com.cookyuu.ecms_server.global.exception.domain.ECMSSellerException;
import com.cookyuu.ecms_server.global.utils.CookieUtils;
import com.cookyuu.ecms_server.global.utils.RedisUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final SellerService sellerService;
    private final RedisUtils redisUtil;
    private final CookieUtils cookieUtils;

    @Transactional
    public Long registerProduct(UserDetails user, RegisterProductDto.Request productInfo) {
        Seller seller = sellerService.findSellerById(Long.parseLong(user.getUsername()));
        Category category = categoryService.findByName(productInfo.getCategoryName());
        Product registerProduct = Product.of(
                productInfo.getName(),
                productInfo.getDescription(),
                productInfo.getPrice(),
                productInfo.getStockQuantity(),
                category,
                seller);
        Product product = productRepository.save(registerProduct);
        log.info("[RegisterProduct] Product registration OK!");
        return product.getId();
    }

    @Transactional
    public void updateProduct(Long productId, UserDetails user, UpdateProductDto.Request productInfo) {
        productInfo.chkAllNull();
        Product product = findProductById(productId);
        Long sellerId = Long.parseLong(user.getUsername());
        if (!isProductOwnedBySeller(product, sellerId)) {
            throw new ECMSSellerException(ResultCode.PRODUCT_OWNER_UNMATCHED);
        }
        if (!(productInfo.getCategoryName()==null || productInfo.getCategoryName().isBlank())) {
            Category category = categoryService.findByName(productInfo.getCategoryName());
            product.updateInfo(productInfo.getName(), productInfo.getDescription(), productInfo.getPrice(), productInfo.getStockQuantity(), category);
        } else {
            product.updateInfo(productInfo.getName(), productInfo.getDescription(), productInfo.getPrice(), productInfo.getStockQuantity(), null);
        }
        log.info("[UpdateProduct] Product update OK!, productId : {}", productId);
    }

    @Transactional
    public void deleteProduct(Long productId, UserDetails user) {
        Product product = findProductById(productId);
        Long sellerId = Long.parseLong(user.getUsername());
        if (!isProductOwnedBySeller(product, sellerId)) {
            throw new ECMSSellerException(ResultCode.PRODUCT_OWNER_UNMATCHED);
        }
        product.isDeleted();
        product.delete();
        log.info("[DeleteProduct] Product delete OK!, ProductId : {}", productId);
    }

    @Transactional(readOnly = true)
    public Page<SearchProductDto.Response> searchProductList(SearchProductDto.Request searchInfo) {
        return productRepository.searchPageOrderByCreatedAtDesc(searchInfo);
    }

    @Transactional(readOnly = true)
    public FindProductDetailDto findProductDetail(Long productId, HttpServletRequest request, HttpServletResponse response) {
        validatePostView(productId, request, response);
        return productRepository.findProductDetail(productId);
    }

    public Product findProductById(Long id) {
        return productRepository.findById(id).orElseThrow(ECMSProductException::new);
    }

    private boolean isProductOwnedBySeller(Product product, Long sellerId) {
        log.info("[CheckProductOwner] Check product owner, ProductId : {}, SellerId : {}", product.getId(), sellerId);
        return product.getSeller().getId().equals(sellerId);
    }

    private void validatePostView(Long productId, HttpServletRequest request, HttpServletResponse response) {
        log.debug("[Product::Detail] Validate post view product in cookie.");
        int postViewCookieDuration = 60*60*3;
        Cookie oldCookie = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CookieCode.POST_VIEW.getKey())) {
                    oldCookie = cookie;
                }
            }
        }

        if (oldCookie != null) {
            if (!oldCookie.getValue().contains("["+ productId.toString() +"]")) {
                increaseProductHits(productId);
                oldCookie.setValue(oldCookie.getValue() + "_[" + productId + "]");
                oldCookie.setPath("/");
                oldCookie.setMaxAge(postViewCookieDuration);
                response.addCookie(oldCookie);
            }
        } else {
            increaseProductHits(productId);
            Cookie newCookie = cookieUtils.setCookieExpire(CookieCode.POST_VIEW, "[" + productId + "]", postViewCookieDuration);
            response.addCookie(newCookie);
        }
    }

    private void increaseProductHits(Long productId) {
        log.debug("[Product::Detail] Product hit count increase");
        if (!redisUtil.hasKey(RedisKeyCode.PRODUCT_HIT_COUNT.getSeparator()+productId)) {
            redisUtil.setHashCountData(RedisKeyCode.PRODUCT_HIT_COUNT.getSeparator()+productId);
        } else {
            redisUtil.increaseCount(RedisKeyCode.PRODUCT_HIT_COUNT.getSeparator()+productId);
        }
    }

}