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

    public String generateShipmentNumber() {
        String shipmentNumber = generateNumber("SP", DEFAULT_RANDOM_LENGTH);

        log.atDebug()
            .addKeyValue("operation", "generateShipmentNumber")
            .addKeyValue(SHIPMENT_NUMBER, shipmentNumber)
            .log("Shipment number generated");

        return shipmentNumber;
    }

    public String generateCouponNumber(CouponCode couponCode) {
        String couponNumber = generateNumber("CP" + couponCode.getCode(), DEFAULT_RANDOM_LENGTH);

        log.atDebug()
            .addKeyValue("operation", "generateCouponNumber")
            .addKeyValue(COUPON_NUMBER, couponNumber)
            .addKeyValue("coupon_code", couponCode.name())
            .log("Coupon number generated");

        return couponNumber;
    }

    private String generateNumber(String prefix, int randomLength) {
        StringBuilder sb = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        sb.append(prefix).append(timestamp);
        appendRandomDigits(sb, randomLength);
        return sb.toString();
    }

    private String generateWithMiddleSuffix(String prefix, String middleSuffix, int randomLength) {
        StringBuilder sb = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        sb.append(prefix).append(timestamp).append(middleSuffix);
        appendRandomDigits(sb, randomLength);
        return sb.toString();
    }

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

    private void appendRandomDigits(StringBuilder sb, int length) {
        for (int i = 0; i < length; i++) {
            sb.append((int) (Math.random() * RANDOM_DIGIT_BOUND));
        }
    }
}
