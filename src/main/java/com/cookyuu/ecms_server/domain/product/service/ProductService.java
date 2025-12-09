package com.cookyuu.ecms_server.domain.product.service;

import com.cookyuu.ecms_server.domain.product.dto.FindProductDetailDto;
import com.cookyuu.ecms_server.domain.product.dto.RegisterProductDto;
import com.cookyuu.ecms_server.domain.product.dto.SearchProductDto;
import com.cookyuu.ecms_server.domain.product.dto.UpdateProductDto;
import com.cookyuu.ecms_server.domain.product.entity.Category;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.domain.product.logging.ProductLogHelper;
import com.cookyuu.ecms_server.domain.product.repository.ProductRepository;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import com.cookyuu.ecms_server.domain.seller.service.SellerService;
import com.cookyuu.ecms_server.common.enums.CookieCode;
import com.cookyuu.ecms_server.common.enums.RedisKeyCode;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.utils.CookieUtils;
import com.cookyuu.ecms_server.common.utils.RedisUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private static final int POST_VIEW_COOKIE_DURATION_SECONDS = 60 * 3;

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final SellerService sellerService;
    private final RedisUtils redisUtil;
    private final CookieUtils cookieUtils;
    private final ProductLogHelper productLogHelper;

    @Transactional
    public Long registerProduct(UserDetails user, RegisterProductDto.Request productInfo) {
        try {
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
            productLogHelper.logProductRegistered(product.getId(), product.getName(), seller.getId(),
                category.getName(), product.getPrice(), product.getStockQuantity());
            return product.getId();
        } catch (Exception e) {
            productLogHelper.logProductRegistrationFailed(Long.parseLong(user.getUsername()),
                ResultCode.PRODUCT_EXISTS_ALREADY, e);
            throw new BusinessException(ResultCode.PRODUCT_EXISTS_ALREADY, e);
        }
    }

    @Transactional
    @CacheEvict(
            value = "routes",
            key = "'product:id:' + T(String).valueOf(#productId)"
    )
    public void updateProduct(Long productId, UserDetails user, UpdateProductDto.Request productInfo) {
        productInfo.chkAllNull();
        Product product = findProductById(productId);
        Long sellerId = Long.parseLong(user.getUsername());
        if (!isProductOwnedBySeller(product, sellerId)) {
            throw new BusinessException(ResultCode.PRODUCT_OWNER_UNMATCHED);
        }
        if (!(productInfo.getCategoryName()==null || productInfo.getCategoryName().isBlank())) {
            Category category = categoryService.findByName(productInfo.getCategoryName());
            product.updateInfo(productInfo.getName(), productInfo.getDescription(), productInfo.getPrice(), productInfo.getStockQuantity(), category);
        } else {
            product.updateInfo(productInfo.getName(), productInfo.getDescription(), productInfo.getPrice(), productInfo.getStockQuantity(), null);
        }
        productLogHelper.logProductUpdated(productId, sellerId);
    }
    @Transactional
    public void deleteProduct(Long productId, UserDetails user) {
        Product product = findProductById(productId);
        Long sellerId = Long.parseLong(user.getUsername());
        if (!isProductOwnedBySeller(product, sellerId)) {
            throw new BusinessException(ResultCode.PRODUCT_OWNER_UNMATCHED);
        }
        product.validateNotDeleted();
        product.delete();
        productLogHelper.logProductDeleted(productId, sellerId);
    }

    @Transactional(readOnly = true)
    public Page<SearchProductDto.Response> searchProductList(SearchProductDto.Request searchInfo) {
        return productRepository.searchPageOrderByCreatedAtDesc(searchInfo);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "routes",
            key = "'product:id:' + T(String).valueOf(#productId)"
    )
    public FindProductDetailDto findProductDetail(Long productId, HttpServletRequest request, HttpServletResponse response) {
        validatePostView(productId, request, response);
        return productRepository.findProductDetail(productId);
    }

    @Transactional
    public void applyHitCount(Long productId, int hitCount) {
        Product product = findProductById(productId);
        product.applyHitCount(hitCount);
    }

    public Product findProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new BusinessException(ResultCode.PRODUCT_NOT_FOUND));
    }

    public Product findProductByIdWithLock(Long id) {
        return productRepository.findByIdWithLock(id).orElseThrow(() -> new BusinessException(ResultCode.PRODUCT_NOT_FOUND));
    }

    public List<Product> findProductsByIdInWithLock(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return productRepository.findByIdInWithLock(ids);
    }

    private boolean isProductOwnedBySeller(Product product, Long sellerId) {
        productLogHelper.logCheckProductOwnership(product.getId(), sellerId);
        return product.getSeller().getId().equals(sellerId);
    }

    private void validatePostView(Long productId, HttpServletRequest request, HttpServletResponse response) {
        productLogHelper.logValidatePostView(productId);
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
                oldCookie.setMaxAge(POST_VIEW_COOKIE_DURATION_SECONDS);
                response.addCookie(oldCookie);
            }
        } else {
            increaseProductHits(productId);
            Cookie newCookie = cookieUtils.setCookieExpire(CookieCode.POST_VIEW, "[" + productId + "]", POST_VIEW_COOKIE_DURATION_SECONDS);
            response.addCookie(newCookie);
        }
    }

    private void increaseProductHits(Long productId) {
        productLogHelper.logProductViewed(productId);
        if (!redisUtil.hasKey(RedisKeyCode.PRODUCT_HIT_COUNT.getSeparator()+productId)) {
            redisUtil.setHashCountData(RedisKeyCode.PRODUCT_HIT_COUNT.getSeparator()+productId);
        } else {
            redisUtil.increaseCount(RedisKeyCode.PRODUCT_HIT_COUNT.getSeparator()+productId);
        }
    }
}