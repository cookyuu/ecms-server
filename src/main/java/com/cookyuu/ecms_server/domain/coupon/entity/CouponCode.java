package com.cookyuu.ecms_server.domain.coupon.entity;

import lombok.Getter;

@Getter
public enum CouponCode {
    FIVE_PERCENT_DISCOUNT("A", 5),
    SEVEN_PERCENT_DISCOUNT("B", 7),
    TEN__PERCENT_DISCOUNT("C", 10),
    FIFTEEN_PERCENT_DISCOUNT("D", 15),
    TWENTY_PERCENT_DISCOUNT("E", 20),
    THIRTY_PERCENT_DISCOUNT("F", 30),
    FORTY_PERCENT_DISCOUNT("G", 40),
    HALF_PERCENT_DISCOUNT("H", 50),
    SIXTY_PERCENT_DISCOUNT("I", 60),
    SEVENTY_PERCENT_DISCOUNT("J", 70),
    EIGHTY_PERCENT_DISCOUNT("K", 60),
    NIGHNTY_PERCENT_DISCOUNT("L", 90),
    FIX_PRICE_DISCOUNT("M", 0),
    NO_COUPON("N", 0);

    private String code;
    private Integer percent;

    CouponCode(String code, Integer percent) {
        this.code = code;
        this.percent = percent;
    }
}
