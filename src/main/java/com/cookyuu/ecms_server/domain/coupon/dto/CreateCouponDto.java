package com.cookyuu.ecms_server.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CreateCouponDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String name;
        private String startAt;
        private String expiredAt;
        private String couponCode;
        private Integer quantity;
        private Integer discountPrice;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private String couponNumber;
    }
}
