package com.cookyuu.ecms_server.domain.product.repository;

import com.cookyuu.ecms_server.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
