package com.cookyuu.ecms_server.domain.payment.service;

import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.domain.order.entity.OrderLine;
import com.cookyuu.ecms_server.domain.order.enums.OrderStatus;
import com.cookyuu.ecms_server.domain.order.service.OrderService;
import com.cookyuu.ecms_server.domain.payment.dto.CancelPaymentDto;
import com.cookyuu.ecms_server.domain.payment.dto.CreatePaymentDto;
import com.cookyuu.ecms_server.domain.payment.dto.PaymentDetailDto;
import com.cookyuu.ecms_server.domain.payment.entity.Payment;
import com.cookyuu.ecms_server.domain.payment.enums.PaymentMethod;
import com.cookyuu.ecms_server.domain.payment.repository.PaymentRepository;
import com.cookyuu.ecms_server.common.enums.RedisKeyCode;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.utils.JwtUtils;
import com.cookyuu.ecms_server.common.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private static final int PAYMENT_NUMBER_EXPIRATION_SECONDS = 61;
    private static final int PAYMENT_NUMBER_RANDOM_SUFFIX_LENGTH = 5;
    private static final int RANDOM_DIGIT_BOUND = 10;

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final RedisUtils redisUtils;

    @Transactional
    public CreatePaymentDto.ResponseServ createPayment(UserDetails user, CreatePaymentDto.Request paymentInfo) {
        Order order = orderService.findOrderByOrderNumberWithAll(paymentInfo.getOrderNumber());
        checkPossiblePayment(order, Long.parseLong(user.getUsername()));
        String paymentNumber = createAndSavePaymentNumberInRedis(paymentInfo.getPaymentMethod());

        List<OrderLine> orderLines = order.getOrderLines();
        if (paymentInfo.getPaymentPrice().equals(order.getTotalPrice())) {
            List<Payment> paymentList = new ArrayList<>();
            for (OrderLine orderLine : orderLines) {
                paymentList.add(paymentInfo.successPayment(order.getBuyer().getId(), orderLine.getProduct().getSeller().getId(), paymentNumber));
            }
            paymentRepository.saveAll(paymentList);
            order.successPayment();
            log.debug("[Payment::CreatePayment] Save Payment success info.");
            return CreatePaymentDto.ResponseServ.toDto(paymentNumber);
        } else {
            order.failPayment(ResultCode.PAYMENT_PRICE_UNMATCHED.getMessage());
            log.debug("[Payment::CreatePayment] Save Payment fail info.");
            return CreatePaymentDto.ResponseServ.toDto(paymentNumber, ResultCode.PAYMENT_PRICE_UNMATCHED);
        }
    }

    @Transactional
    public CancelPaymentDto.Response cancelPayment(UserDetails user, CancelPaymentDto.Request paymentInfo) {
        log.debug("[Payment::cancel] Request Info. orderNumber : {}, paymentNumber : {}, cancelReason : {}",
                paymentInfo.getOrderNumber(), paymentInfo.getPaymentNumber(), paymentInfo.getCancelReason());
        Order order = orderService.findOrderByOrderNumberWithBuyer(paymentInfo.getOrderNumber());
        checkPossiblePaymentCancel(order, Long.parseLong(user.getUsername()));
        Payment payment = findPaymentByPaymentNumber(paymentInfo.getPaymentNumber());
        payment.cancel(paymentInfo.getCancelReason());
        order.cancelPayment();
        log.debug("[Payment::cancel] Cancel Payment process, OK");
        return CancelPaymentDto.Response.toDto(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDetailDto> getPaymentDetail(UserDetails user, String paymentNumber) {
        log.debug("[Payment::getDetail] Request paymentNumber : {}", paymentNumber);
        String reqUserRole = JwtUtils.getRoleFromUserDetails(user);
        Long reqUserId = Long.parseLong(user.getUsername());
        List<PaymentDetailDto> resPaymentDetail = getPaymentInfo(paymentNumber);
        if (reqUserRole.equals("ROLE_USER")) {
            boolean hasAccess = resPaymentDetail.stream()
                    .anyMatch(paymentDetail -> paymentDetail.getBuyerId().equals(reqUserId));
            if (!hasAccess) {
                throw new BusinessException(ResultCode.PAYMENT_INACCESSIBLE_DETAIL);
            }
        } else if (reqUserRole.equals("ROLE_SELLER")) {
            resPaymentDetail.removeIf(paymentDetail -> !paymentDetail.getSellerId().equals(reqUserId));
            if (resPaymentDetail.isEmpty()) {
                throw new BusinessException(ResultCode.PAYMENT_INACCESSIBLE_DETAIL);
            }
        } else if (reqUserRole.equals("ROLE_ADMIN")) {
            log.debug("[Payment::getDetail] User role : {}, OK", reqUserRole);
        } else {
            throw new BusinessException(ResultCode.PAYMENT_INACCESSIBLE_DETAIL);
        }

        log.debug("[Payment::getDetail] Get payment detail process, OK");
        return resPaymentDetail;
    }

    private List<PaymentDetailDto> getPaymentInfo(String paymentNumber) {
        return paymentRepository.getPaymentDetail(paymentNumber);
    }

    private String createAndSavePaymentNumberInRedis(PaymentMethod paymentMethod) {
        String paymentNumber = createPaymentNumber(paymentMethod);
        while (redisUtils.getData(RedisKeyCode.PAYMENT_NUMBER.getSeparator()+paymentNumber) != null) {
            paymentNumber = createPaymentNumber(paymentMethod);
            log.debug("[Payment::CreatePayment] Created payment number is duplicated, Payment Number : {}", paymentNumber);
        }

        try {
            String redisValueOfPayment = "true";
            redisUtils.setDataExpire(RedisKeyCode.PAYMENT_NUMBER.getSeparator()+paymentNumber, redisValueOfPayment, PAYMENT_NUMBER_EXPIRATION_SECONDS);
            log.debug("[Payment::CreatePayment] Insert payment number in redis");

        } catch (Exception e) {
            redisUtils.deleteData(RedisKeyCode.PAYMENT_NUMBER.getSeparator() + paymentNumber);
            log.error("[Payment::Error] Transaction failed, rollback Redis key. Payment Number : {}", paymentNumber);
            throw e;
        }
        return paymentNumber;
    }

    private String createPaymentNumber(PaymentMethod paymentMethod) {
        StringBuilder sb = new StringBuilder();
        String formatDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
        sb.append(paymentMethod.getCode()).append(formatDate);
        for (int i = 0; i < PAYMENT_NUMBER_RANDOM_SUFFIX_LENGTH; i++) {
            int random = (int) (Math.random() * RANDOM_DIGIT_BOUND);
            sb.append(random);
        }
        return sb.toString();
    }

    private void checkPossiblePayment(Order order, Long paymentUserId) {
        compareToBuyerPaymentUser(order.getBuyer().getId(), paymentUserId);
        List<OrderStatus> paymentPossibleOrderStatuses = new ArrayList<>();
        paymentPossibleOrderStatuses.add(OrderStatus.ORDER_COMPLETE);
        paymentPossibleOrderStatuses.add(OrderStatus.PAYMENT_FAIL);

        if (!paymentPossibleOrderStatuses.contains(order.getStatus())) {
            throw new BusinessException(ResultCode.PAYMENT_IMPOSSIBLE_STATUS);
        }
        log.debug("[Payment::Check] Check possible order status for payment, OK");

    }

    private void checkPossiblePaymentCancel(Order order, Long paymentUserId) {
        compareToBuyerPaymentUser(order.getBuyer().getId(), paymentUserId);
        if (!order.getStatus().equals(OrderStatus.PAYMENT_COMPLETE)) {
            throw new BusinessException(ResultCode.PAYMENT_IMPOSSIBLE_STATUS);
        }
        log.debug("[Payment::Check] Check possible order status for payment cancel, OK");
    }

    private Payment findPaymentByPaymentNumber(String paymentNumber) {
        return paymentRepository.findByPaymentNumber(paymentNumber).orElseThrow(() -> new BusinessException(ResultCode.PAYMENT_NOT_FOUND));
    }

    private void compareToBuyerPaymentUser(Long buyerId, Long paymentUserId) {
        log.debug("[Payment::Check] Compare to buyer and payment user");
        if (!Objects.equals(buyerId, paymentUserId)) {
            throw new BusinessException(ResultCode.PAYMENT_BUYER_UNMATCHED);
        }
    }
}
