package com.cookyuu.ecms_server.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

public class CreateCouponDto {

    @Getter
    @AllArgsConstructor
    public static class Request {
        private String name;
        private LocalDate expiredAt;
        private String couponCode;
        private Integer discountPrice;
    }

    @Builder
    @AllArgsConstructor
    public static class Response {
        private String couponNumber;
    }
}
