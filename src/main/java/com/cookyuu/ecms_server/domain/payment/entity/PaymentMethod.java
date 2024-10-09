package com.cookyuu.ecms_server.domain.payment.entity;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CARD("CARD"), KAKAO_PAY("KKOP"), NAVER_PAY("NAVP"), PAYCO("PAYC"), PAYPAL("PAYP");

    private String code;

    PaymentMethod(String code) {
        this.code = code;
    }
}
