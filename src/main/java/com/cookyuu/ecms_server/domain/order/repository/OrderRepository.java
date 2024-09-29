package com.cookyuu.ecms_server.domain.order.repository;

import com.cookyuu.ecms_server.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Object> findByOrderNumber(String orderNumber);
}
