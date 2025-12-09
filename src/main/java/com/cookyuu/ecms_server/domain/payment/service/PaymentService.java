package com.cookyuu.ecms_server.domain.payment.service;

import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.domain.order.entity.OrderLine;
import com.cookyuu.ecms_server.domain.order.enums.OrderStatus;
import com.cookyuu.ecms_server.domain.order.service.OrderService;
import com.cookyuu.ecms_server.domain.payment.dto.CancelPaymentDto;
import com.cookyuu.ecms_server.domain.payment.dto.CreatePaymentDto;
import com.cookyuu.ecms_server.domain.payment.dto.PaymentDetailDto;
import com.cookyuu.ecms_server.domain.payment.entity.Payment;
import com.cookyuu.ecms_server.domain.payment.logging.PaymentLogHelper;
import com.cookyuu.ecms_server.domain.payment.repository.PaymentRepository;
import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.generator.BusinessNumberGenerator;
import com.cookyuu.ecms_server.common.security.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final AuthorizationService authorizationService;
    private final BusinessNumberGenerator businessNumberGenerator;
    private final PaymentLogHelper paymentLogHelper;

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

            paymentLogHelper.logPaymentCompleted(userId, order.getId(), order.getOrderNumber(),
                paymentNumber, paymentInfo.getPaymentMethod().name(), paymentInfo.getPaymentPrice(),
                System.currentTimeMillis() - startTime);

            return CreatePaymentDto.ResponseServ.toDto(paymentNumber);
        } else {
            order.failPayment(ResultCode.PAYMENT_PRICE_UNMATCHED.getMessage());

            paymentLogHelper.logPaymentFailedAmountMismatch(userId, order.getId(), order.getOrderNumber(),
                paymentNumber, paymentInfo.getPaymentMethod().name(), paymentInfo.getPaymentPrice(),
                order.getTotalPrice(), ResultCode.PAYMENT_PRICE_UNMATCHED, System.currentTimeMillis() - startTime);

            return CreatePaymentDto.ResponseServ.toDto(paymentNumber, ResultCode.PAYMENT_PRICE_UNMATCHED);
        }
    }

    @Transactional
    public CancelPaymentDto.Response cancelPayment(UserDetails user, CancelPaymentDto.Request paymentInfo) {
        long startTime = System.currentTimeMillis();
        Long userId = Long.parseLong(user.getUsername());

        paymentLogHelper.logPaymentCancellationRequested(userId, paymentInfo.getOrderNumber(),
            paymentInfo.getPaymentNumber(), paymentInfo.getCancelReason());

        Order order = orderService.findOrderByOrderNumberWithBuyer(paymentInfo.getOrderNumber());
        checkPossiblePaymentCancel(order, userId);
        Payment payment = findPaymentByPaymentNumber(paymentInfo.getPaymentNumber());
        payment.cancel(paymentInfo.getCancelReason());
        order.cancelPayment();

        paymentLogHelper.logPaymentCancelled(userId, paymentInfo.getOrderNumber(),
            paymentInfo.getPaymentNumber(), paymentInfo.getCancelReason(), System.currentTimeMillis() - startTime);

        return CancelPaymentDto.Response.toDto(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDetailDto> getPaymentDetail(UserDetails user, String paymentNumber) {
        Long reqUserId = Long.parseLong(user.getUsername());
        RoleType userRole = authorizationService.getUserRole(user);

        paymentLogHelper.logPaymentDetailFetch(reqUserId, userRole.name(), paymentNumber);

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

        paymentLogHelper.logPaymentDetailFetched(reqUserId, paymentNumber);
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
            paymentLogHelper.logPaymentValidationFailed(order.getId(), order.getStatus().name(),
                ResultCode.PAYMENT_IMPOSSIBLE_STATUS);
            throw new BusinessException(ResultCode.PAYMENT_IMPOSSIBLE_STATUS);
        }
        paymentLogHelper.logPaymentValidationPassed(order.getId(), order.getStatus().name());
    }

    private void checkPossiblePaymentCancel(Order order, Long paymentUserId) {
        authorizationService.validateResourceOwnership(
            paymentUserId,
            order.getBuyer().getId(),
            ResultCode.PAYMENT_BUYER_UNMATCHED
        );

        if (!order.getStatus().equals(OrderStatus.PAYMENT_COMPLETE)) {
            paymentLogHelper.logPaymentCancellationValidationFailed(order.getId(),
                order.getStatus().name(), ResultCode.PAYMENT_IMPOSSIBLE_STATUS);
            throw new BusinessException(ResultCode.PAYMENT_IMPOSSIBLE_STATUS);
        }
        paymentLogHelper.logPaymentCancellationValidationPassed(order.getId(), order.getStatus().name());
    }

    private Payment findPaymentByPaymentNumber(String paymentNumber) {
        return paymentRepository.findByPaymentNumber(paymentNumber).orElseThrow(() -> new BusinessException(ResultCode.PAYMENT_NOT_FOUND));
    }
}
