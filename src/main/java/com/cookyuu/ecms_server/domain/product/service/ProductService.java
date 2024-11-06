package com.cookyuu.ecms_server.domain.product.service;

import com.cookyuu.ecms_server.domain.product.dto.RegisterProductDto;
import com.cookyuu.ecms_server.domain.product.dto.SearchProductDto;
import com.cookyuu.ecms_server.domain.product.dto.UpdateProductDto;
import com.cookyuu.ecms_server.domain.product.entity.Category;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.domain.product.repository.ProductRepository;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import com.cookyuu.ecms_server.domain.seller.service.SellerService;
import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.domain.ECMSProductException;
import com.cookyuu.ecms_server.global.exception.domain.ECMSSellerException;
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

    public Product findProductById(Long id) {
        return productRepository.findById(id).orElseThrow(ECMSProductException::new);
    }

    private boolean isProductOwnedBySeller(Product product, Long sellerId) {
        log.info("[CheckProductOwner] Check product owner, ProductId : {}, SellerId : {}", product.getId(), sellerId);
        return product.getSeller().getId().equals(sellerId);
    }
}