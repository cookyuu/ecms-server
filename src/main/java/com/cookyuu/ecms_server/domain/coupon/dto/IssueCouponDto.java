package com.cookyuu.ecms_server.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class IssueCouponDto {

    @Getter
    @AllArgsConstructor
    public static class Request {
        private String couponNumber;
    }

}
