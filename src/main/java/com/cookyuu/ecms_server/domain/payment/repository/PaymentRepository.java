package com.cookyuu.ecms_server.domain.payment.repository;

import com.cookyuu.ecms_server.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
