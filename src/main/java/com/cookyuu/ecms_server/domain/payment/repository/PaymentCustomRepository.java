package com.cookyuu.ecms_server.domain.payment.repository;

import com.cookyuu.ecms_server.domain.payment.dto.PaymentDetailDto;

import java.util.List;

public interface PaymentCustomRepository {
    List<PaymentDetailDto> getPaymentDetail(String paymentNumber);
}
