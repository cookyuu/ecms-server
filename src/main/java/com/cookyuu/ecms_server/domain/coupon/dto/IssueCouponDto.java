package com.cookyuu.ecms_server.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class IssueCouponDto {

    @Getter
    @AllArgsConstructor
    public static class Request {
        private String couponNumber;
    }

    @Builder
    @AllArgsConstructor
    public static class Response {
        private String couponNumber;
    }

}
