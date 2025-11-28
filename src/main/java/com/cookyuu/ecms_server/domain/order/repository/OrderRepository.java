package com.cookyuu.ecms_server.domain.order.repository;

import com.cookyuu.ecms_server.domain.order.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderCustomRepository {
    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.buyer " +
           "WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberWithBuyer(@Param("orderNumber") String orderNumber);

    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.orderLines ol " +
           "LEFT JOIN FETCH ol.product " +
           "LEFT JOIN FETCH o.buyer " +
           "WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberWithProducts(@Param("orderNumber") String orderNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.orderLines ol " +
           "LEFT JOIN FETCH ol.product " +
           "LEFT JOIN FETCH o.buyer " +
           "WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberWithProductsForUpdate(@Param("orderNumber") String orderNumber);

    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.orderLines ol " +
           "LEFT JOIN FETCH ol.product p " +
           "LEFT JOIN FETCH p.seller " +
           "LEFT JOIN FETCH o.buyer " +
           "WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberWithAll(@Param("orderNumber") String orderNumber);
}
