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
import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.generator.BusinessNumberGenerator;
import com.cookyuu.ecms_server.common.security.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final AuthorizationService authorizationService;
    private final BusinessNumberGenerator businessNumberGenerator;

    @Transactional
    public CreatePaymentDto.ResponseServ createPayment(UserDetails user, CreatePaymentDto.Request paymentInfo) {
        long startTime = System.currentTimeMillis();
        Long userId = Long.parseLong(user.getUsername());

        Order order = orderService.findOrderByOrderNumberWithAll(paymentInfo.getOrderNumber());
        checkPossiblePayment(order, userId);
        String paymentNumber = businessNumberGenerator.generatePaymentNumber(paymentInfo.getPaymentMethod());

        List<OrderLine> orderLines = order.getOrderLines();
        if (paymentInfo.getPaymentPrice().equals(order.getTotalPrice())) {
            List<Payment> paymentList = new ArrayList<>();
            for (OrderLine orderLine : orderLines) {
                paymentList.add(paymentInfo.successPayment(order.getBuyer().getId(), orderLine.getProduct().getSeller().getId(), paymentNumber));
            }
            paymentRepository.saveAll(paymentList);
            order.successPayment();

            log.atInfo()
                .addKeyValue(EVENT, PAYMENT_COMPLETED)
                .addKeyValue(USER_ID, userId)
                .addKeyValue(ORDER_ID, order.getId())
                .addKeyValue(ORDER_NUMBER, order.getOrderNumber())
                .addKeyValue(PAYMENT_NUMBER, paymentNumber)
                .addKeyValue(PAYMENT_METHOD, paymentInfo.getPaymentMethod().name())
                .addKeyValue(PAYMENT_AMOUNT, paymentInfo.getPaymentPrice())
                .addKeyValue(DURATION_MS, System.currentTimeMillis() - startTime)
                .log("Payment completed successfully");

            return CreatePaymentDto.ResponseServ.toDto(paymentNumber);
        } else {
            order.failPayment(ResultCode.PAYMENT_PRICE_UNMATCHED.getMessage());

            log.atWarn()
                .addKeyValue(EVENT, PAYMENT_FAILED)
                .addKeyValue(USER_ID, userId)
                .addKeyValue(ORDER_ID, order.getId())
                .addKeyValue(ORDER_NUMBER, order.getOrderNumber())
                .addKeyValue(PAYMENT_NUMBER, paymentNumber)
                .addKeyValue(PAYMENT_METHOD, paymentInfo.getPaymentMethod().name())
                .addKeyValue(REQUESTED_AMOUNT, paymentInfo.getPaymentPrice())
                .addKeyValue(EXPECTED_AMOUNT, order.getTotalPrice())
                .addKeyValue(ERROR_CODE, ResultCode.PAYMENT_PRICE_UNMATCHED.getCode())
                .addKeyValue(ERROR_MESSAGE, "Payment amount mismatch")
                .addKeyValue(DURATION_MS, System.currentTimeMillis() - startTime)
                .log("Payment failed - amount mismatch");

            return CreatePaymentDto.ResponseServ.toDto(paymentNumber, ResultCode.PAYMENT_PRICE_UNMATCHED);
        }
    }

    @Transactional
    public CancelPaymentDto.Response cancelPayment(UserDetails user, CancelPaymentDto.Request paymentInfo) {
        long startTime = System.currentTimeMillis();
        Long userId = Long.parseLong(user.getUsername());

        log.atDebug()
            .addKeyValue(EVENT, PAYMENT_CANCELLED)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(ORDER_NUMBER, paymentInfo.getOrderNumber())
            .addKeyValue(PAYMENT_NUMBER, paymentInfo.getPaymentNumber())
            .addKeyValue(CANCEL_REASON, paymentInfo.getCancelReason())
            .log("Payment cancellation requested");

        Order order = orderService.findOrderByOrderNumberWithBuyer(paymentInfo.getOrderNumber());
        checkPossiblePaymentCancel(order, userId);
        Payment payment = findPaymentByPaymentNumber(paymentInfo.getPaymentNumber());
        payment.cancel(paymentInfo.getCancelReason());
        order.cancelPayment();

        log.atInfo()
            .addKeyValue(EVENT, PAYMENT_CANCELLED)
            .addKeyValue(USER_ID, userId)
            .addKeyValue(ORDER_NUMBER, paymentInfo.getOrderNumber())
            .addKeyValue(PAYMENT_NUMBER, paymentInfo.getPaymentNumber())
            .addKeyValue(CANCEL_REASON, paymentInfo.getCancelReason())
            .addKeyValue(DURATION_MS, System.currentTimeMillis() - startTime)
            .log("Payment cancelled successfully");

        return CancelPaymentDto.Response.toDto(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDetailDto> getPaymentDetail(UserDetails user, String paymentNumber) {
        Long reqUserId = Long.parseLong(user.getUsername());
        RoleType userRole = authorizationService.getUserRole(user);

        log.atDebug()
            .addKeyValue("operation", "getPaymentDetail")
            .addKeyValue(USER_ID, reqUserId)
            .addKeyValue(USER_ROLE, userRole.name())
            .addKeyValue(PAYMENT_NUMBER, paymentNumber)
            .log("Fetching payment detail");

        List<PaymentDetailDto> resPaymentDetail = getPaymentInfo(paymentNumber);

        if (userRole == RoleType.USER) {
            authorizationService.validateUserAccess(
                user,
                resPaymentDetail,
                reqUserId,
                paymentDetail -> paymentDetail.getBuyerId().equals(reqUserId),
                ResultCode.PAYMENT_INACCESSIBLE_DETAIL
            );
        } else if (userRole == RoleType.SELLER) {
            resPaymentDetail = authorizationService.filterResourcesByRole(
                user,
                resPaymentDetail,
                reqUserId,
                paymentDetail -> paymentDetail.getSellerId().equals(reqUserId),
                ResultCode.PAYMENT_INACCESSIBLE_DETAIL
            );
        }

        log.atDebug()
            .addKeyValue("operation", "getPaymentDetail")
            .addKeyValue(USER_ID, reqUserId)
            .addKeyValue(PAYMENT_NUMBER, paymentNumber)
            .log("Payment detail fetched successfully");
        return resPaymentDetail;
    }

    private List<PaymentDetailDto> getPaymentInfo(String paymentNumber) {
        return paymentRepository.getPaymentDetail(paymentNumber);
    }

    private void checkPossiblePayment(Order order, Long paymentUserId) {
        authorizationService.validateResourceOwnership(
            paymentUserId,
            order.getBuyer().getId(),
            ResultCode.PAYMENT_BUYER_UNMATCHED
        );

        List<OrderStatus> paymentPossibleOrderStatuses = new ArrayList<>();
        paymentPossibleOrderStatuses.add(OrderStatus.ORDER_COMPLETE);
        paymentPossibleOrderStatuses.add(OrderStatus.PAYMENT_FAIL);

        if (!paymentPossibleOrderStatuses.contains(order.getStatus())) {
            log.atWarn()
                .addKeyValue(EVENT, VALIDATION_ERROR)
                .addKeyValue(ORDER_ID, order.getId())
                .addKeyValue(ORDER_STATUS, order.getStatus().name())
                .addKeyValue(ERROR_CODE, ResultCode.PAYMENT_IMPOSSIBLE_STATUS.getCode())
                .log("Payment validation failed - invalid order status");
            throw new BusinessException(ResultCode.PAYMENT_IMPOSSIBLE_STATUS);
        }
        log.atDebug()
            .addKeyValue("operation", "checkPossiblePayment")
            .addKeyValue(ORDER_ID, order.getId())
            .addKeyValue(ORDER_STATUS, order.getStatus().name())
            .log("Payment validation passed");

    }

    private void checkPossiblePaymentCancel(Order order, Long paymentUserId) {
        authorizationService.validateResourceOwnership(
            paymentUserId,
            order.getBuyer().getId(),
            ResultCode.PAYMENT_BUYER_UNMATCHED
        );

        if (!order.getStatus().equals(OrderStatus.PAYMENT_COMPLETE)) {
            log.atWarn()
                .addKeyValue(EVENT, VALIDATION_ERROR)
                .addKeyValue(ORDER_ID, order.getId())
                .addKeyValue(ORDER_STATUS, order.getStatus().name())
                .addKeyValue(ERROR_CODE, ResultCode.PAYMENT_IMPOSSIBLE_STATUS.getCode())
                .log("Payment cancellation validation failed - invalid order status");
            throw new BusinessException(ResultCode.PAYMENT_IMPOSSIBLE_STATUS);
        }
        log.atDebug()
            .addKeyValue("operation", "checkPossiblePaymentCancel")
            .addKeyValue(ORDER_ID, order.getId())
            .addKeyValue(ORDER_STATUS, order.getStatus().name())
            .log("Payment cancellation validation passed");
    }

    private Payment findPaymentByPaymentNumber(String paymentNumber) {
        return paymentRepository.findByPaymentNumber(paymentNumber).orElseThrow(() -> new BusinessException(ResultCode.PAYMENT_NOT_FOUND));
    }
}
