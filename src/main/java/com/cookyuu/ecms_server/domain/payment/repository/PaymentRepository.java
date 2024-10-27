package com.cookyuu.ecms_server.domain.payment.repository;

import com.cookyuu.ecms_server.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long>, PaymentCustomRepository {
    Optional<Payment> findByPaymentNumber(String paymentNumber);
}
