package com.cookyuu.ecms_server.domain.order.repository;

import com.cookyuu.ecms_server.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
