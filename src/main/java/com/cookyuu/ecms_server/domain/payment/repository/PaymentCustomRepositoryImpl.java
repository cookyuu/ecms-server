package com.cookyuu.ecms_server.domain.payment.repository;

import com.cookyuu.ecms_server.domain.payment.dto.PaymentDetailDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.cookyuu.ecms_server.domain.payment.entity.QPayment.payment;

@RequiredArgsConstructor
public class PaymentCustomRepositoryImpl implements PaymentCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PaymentDetailDto> getPaymentDetail(String paymentNumber) {

        return queryFactory
                .select(Projections.constructor(PaymentDetailDto.class,
                        payment.id.as("paymentId"),
                        payment.paymentNumber,
                        payment.orderNumber,
                        payment.amount,
                        payment.paymentMethod,
                        payment.status.as("paymentStatus"),
                        payment.cancelReason,
                        payment.canceledAt,
                        payment.paymentFailMsg,
                        payment.buyerId,
                        payment.sellerId
                        ))
                .from(payment)
                .where(paymentNumberEq(paymentNumber))
                .fetch();
    }

    private BooleanExpression paymentNumberEq(String paymentNumber) {
        return payment.paymentNumber.eq(paymentNumber);
    }
}
