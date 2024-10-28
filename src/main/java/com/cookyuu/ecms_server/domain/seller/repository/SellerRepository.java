package com.cookyuu.ecms_server.domain.seller.repository;

import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long>, SellerCustomRepository {
    boolean existsByBusinessNumber(String businessNumber);

    boolean existsByLoginId(String loginId);

    Optional<Seller> findByLoginId(String loginId);
}
