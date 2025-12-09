package com.cookyuu.ecms_server.common.generator;

import com.cookyuu.ecms_server.domain.coupon.enums.CouponCode;
import com.cookyuu.ecms_server.domain.order.enums.OrderCode;
import com.cookyuu.ecms_server.domain.payment.enums.PaymentMethod;
import com.cookyuu.ecms_server.common.enums.RedisKeyCode;
import com.cookyuu.ecms_server.common.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessNumberGenerator {

    private static final String DATE_FORMAT = "yyMMddHHmm";
    private static final int DEFAULT_RANDOM_LENGTH = 5;
    private static final int RANDOM_DIGIT_BOUND = 10;
    private static final int NUMBER_EXPIRATION_SECONDS = 61;

    private final RedisUtils redisUtils;

    /**
     * 주문번호 생성
     *
     * @param orderCode 주문 타입 코드
     * @param couponCode 쿠폰 코드
     * @return 생성된 주문번호 (예: ORD24120816120012345)
     */
    public String generateOrderNumber(OrderCode orderCode, CouponCode couponCode) {
        String prefix = orderCode.getCode();
        String suffix = couponCode.getCode();
        String orderNumber = generateWithMiddleSuffix(prefix, suffix, DEFAULT_RANDOM_LENGTH);

        log.atDebug()
            .addKeyValue("operation", "generateOrderNumber")
            .addKeyValue(ORDER_NUMBER, orderNumber)
            .addKeyValue("order_code", orderCode.name())
            .addKeyValue("coupon_code", couponCode.name())
            .log("Order number generated");

        return orderNumber;
    }

    /**
     * 결제번호 생성 (Redis 기반 중복 방지)
     *
     * @param paymentMethod 결제 수단
     * @return 생성된 결제번호 (예: CARD24120816120012345)
     */
    public String generatePaymentNumber(PaymentMethod paymentMethod) {
        String paymentNumber = generateUniqueNumber(
            paymentMethod.getCode(),
            RedisKeyCode.PAYMENT_NUMBER,
            DEFAULT_RANDOM_LENGTH
        );

        log.atDebug()
            .addKeyValue("operation", "generatePaymentNumber")
            .addKeyValue(PAYMENT_NUMBER, paymentNumber)
            .addKeyValue("payment_method", paymentMethod.name())
            .log("Payment number generated");

        return paymentNumber;
    }

    /**
     * 배송번호 생성
     *
     * @return 생성된 배송번호 (예: SP24120816120012345)
     */
    public String generateShipmentNumber() {
        String shipmentNumber = generateNumber("SP", DEFAULT_RANDOM_LENGTH);

        log.atDebug()
            .addKeyValue("operation", "generateShipmentNumber")
            .addKeyValue(SHIPMENT_NUMBER, shipmentNumber)
            .log("Shipment number generated");

        return shipmentNumber;
    }

    /**
     * 쿠폰번호 생성
     *
     * @param couponCode 쿠폰 타입 코드
     * @return 생성된 쿠폰번호 (예: CPM24120816120012345)
     */
    public String generateCouponNumber(CouponCode couponCode) {
        String couponNumber = generateNumber("CP" + couponCode.getCode(), DEFAULT_RANDOM_LENGTH);

        log.atDebug()
            .addKeyValue("operation", "generateCouponNumber")
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue("coupon_code", couponCode.name())
            .log("Coupon number generated");

        return couponNumber;
    }

    /**
     * 기본 번호 생성
     * 포맷: {prefix}{yyMMddHHmm}{랜덤숫자}
     */
    private String generateNumber(String prefix, int randomLength) {
        StringBuilder sb = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        sb.append(prefix).append(timestamp);
        appendRandomDigits(sb, randomLength);
        return sb.toString();
    }

    /**
     * 중간에 suffix가 포함된 번호 생성
     * 포맷: {prefix}{yyMMddHHmm}{middleSuffix}{랜덤숫자}
     */
    private String generateWithMiddleSuffix(String prefix, String middleSuffix, int randomLength) {
        StringBuilder sb = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        sb.append(prefix).append(timestamp).append(middleSuffix);
        appendRandomDigits(sb, randomLength);
        return sb.toString();
    }

    /**
     * Redis를 통한 중복 체크와 함께 고유 번호 생성
     */
    private String generateUniqueNumber(String prefix, RedisKeyCode keyCode, int randomLength) {
        String number;
        int attemptCount = 0;

        do {
            number = generateNumber(prefix, randomLength);
            attemptCount++;

            if (attemptCount > 10) {
                log.atWarn()
                    .addKeyValue(EVENT, SYSTEM_ERROR)
                    .addKeyValue("operation", "generateUniqueNumber")
                    .addKeyValue("attempt_count", attemptCount)
                    .addKeyValue("prefix", prefix)
                    .log("Multiple attempts to generate unique number");
            }
        } while (redisUtils.getData(keyCode.getSeparator() + number) != null);

        // Redis에 번호 저장 (만료 시간 설정)
        redisUtils.setDataExpire(
            keyCode.getSeparator() + number,
            "true",
            NUMBER_EXPIRATION_SECONDS
        );

        log.atDebug()
            .addKeyValue("operation", "generateUniqueNumber")
            .addKeyValue("generated_number", number)
            .addKeyValue("attempt_count", attemptCount)
            .log("Unique number generated and stored in Redis");

        return number;
    }

    /**
     * StringBuilder에 랜덤 숫자 추가
     */
    private void appendRandomDigits(StringBuilder sb, int length) {
        for (int i = 0; i < length; i++) {
            sb.append((int) (Math.random() * RANDOM_DIGIT_BOUND));
        }
    }
}
