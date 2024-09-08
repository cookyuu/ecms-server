package com.cookyuu.ecms_server.domain.product.service;

import com.cookyuu.ecms_server.domain.product.dto.RegisterProductDto;
import com.cookyuu.ecms_server.domain.product.entity.Category;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.domain.product.repository.ProductRepository;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import com.cookyuu.ecms_server.domain.seller.service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void registerProduct(UserDetails user, RegisterProductDto.Request productInfo) {
        log.info("seller id : {}", Long.parseLong(user.getUsername()));
        log.info("category name : {}", productInfo.getCategoryName());
        Seller seller = sellerService.findSellerById(Long.parseLong(user.getUsername()));
        Category category = categoryService.findByName(productInfo.getCategoryName());
        Product registerProduct = Product.of(
                productInfo.getName(),
                productInfo.getDescription(),
                productInfo.getPrice(),
                productInfo.getStockQuantity(),
                category,
                seller);
        productRepository.save(registerProduct);
    }
}