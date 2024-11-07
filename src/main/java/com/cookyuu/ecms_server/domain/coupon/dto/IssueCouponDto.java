package com.cookyuu.ecms_server.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class IssueCouponDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String couponNumber;
    }
}
