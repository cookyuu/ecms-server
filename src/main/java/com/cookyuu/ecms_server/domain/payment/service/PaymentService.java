package com.cookyuu.ecms_server.domain.payment.service;

import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.domain.order.entity.OrderStatus;
import com.cookyuu.ecms_server.domain.order.service.OrderService;
import com.cookyuu.ecms_server.domain.payment.dto.CreatePaymentDto;
import com.cookyuu.ecms_server.domain.payment.entity.PaymentMethod;
import com.cookyuu.ecms_server.domain.payment.repository.PaymentRepository;
import com.cookyuu.ecms_server.global.dto.RedisKeyCode;
import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.domain.ECMSPaymentException;
import com.cookyuu.ecms_server.global.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final RedisUtils redisUtils;

    @Transactional
    public CreatePaymentDto.ResponseServ createPayment(UserDetails user, CreatePaymentDto.Request paymentInfo) {
        Order order = orderService.findOrderByOrderNumber(paymentInfo.getOrderNumber());
        checkPossiblePayment(order, Long.parseLong(user.getUsername()));

        String paymentNumber = createPaymentNumber(paymentInfo.getPaymentMethod());
        while (redisUtils.getData(RedisKeyCode.PAYMENT_NUMBER.getSeparator()+paymentNumber) != null) {
            paymentNumber = createPaymentNumber(paymentInfo.getPaymentMethod());
            log.debug("[Payment::CreatePayment] Created payment number is duplicated, Payment Number : {}", paymentNumber);
        }

        try {
            int paymentNumberExp = 61;
            String redisValueOfPayment = "true";
            redisUtils.setDataExpire(RedisKeyCode.PAYMENT_NUMBER.getSeparator()+paymentNumber, redisValueOfPayment, paymentNumberExp);
            log.debug("[Payment::CreatePayment] Insert payment number in redis");

        } catch (Exception e) {
            redisUtils.deleteData(RedisKeyCode.PAYMENT_NUMBER.getSeparator() + paymentNumber);
            log.error("[Payment::Error] Transaction failed, rollback Redis key. Payment Number : {}", paymentNumber);
            throw e;
        }

        if (paymentInfo.getPaymentPrice().equals(order.getTotalPrice())) {
            paymentRepository.save(paymentInfo.successPayment(paymentNumber));
            order.successPayment();
            log.debug("[Payment::CreatePayment] Save Payment info.");
            return CreatePaymentDto.ResponseServ.toDto(paymentNumber);
        } else {
            paymentRepository.save(paymentInfo.failPayment(paymentNumber, ResultCode.PAYMENT_PRICE_UNMATCHED.getMessage()));
            order.failPayment();
            return CreatePaymentDto.ResponseServ.toDto(paymentNumber, ResultCode.PAYMENT_PRICE_UNMATCHED);
        }
    }

    private String createPaymentNumber(PaymentMethod paymentMethod) {
        StringBuilder sb = new StringBuilder();
        String formatDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
        sb.append(paymentMethod.getCode()).append(formatDate);
        for (int i = 0; i < 5; i++) {
            int random = (int) (Math.random() * 10);
            sb.append(random);
        }
        return sb.toString();
    }

    private void checkPossiblePayment(Order order, Long userId) {
        if (!order.getBuyer().compareMemberId(userId)) {
            throw new ECMSPaymentException(ResultCode.PAYMENT_BUYER_UNMATCHED);
        }
        List<OrderStatus> paymentPossibleOrderStatuses = new ArrayList<>();
        paymentPossibleOrderStatuses.add(OrderStatus.ORDER_COMPLETE);
        paymentPossibleOrderStatuses.add(OrderStatus.PAYMENT_FAIL);

        if (!paymentPossibleOrderStatuses.contains(order.getStatus())) {
            throw new ECMSPaymentException(ResultCode.PAYMENT_IMPOSSIBLE_STATUS);
        }

    }
}
