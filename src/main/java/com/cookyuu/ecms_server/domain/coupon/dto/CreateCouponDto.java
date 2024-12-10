package com.cookyuu.ecms_server.domain.coupon.dto;

import com.cookyuu.ecms_server.domain.coupon.entity.Coupon;
import com.cookyuu.ecms_server.domain.coupon.entity.CouponCode;
import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.domain.ECMSCouponException;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CreateCouponDto {

    @Getter
    @AllArgsConstructor
    public static class Request {
        @NotBlank
        private String name;
        @NotBlank
        private String couponCode;
        private Integer price;

        @NotBlank
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate expiredAt;
        @NotBlank
        private Long stockQuantity;

        private String couponNumber;

        public Coupon toEntity() {
            return Coupon.builder()
                    .name(this.name)
                    .stockQuantity(this.stockQuantity)
                    .price(this.price)
                    .couponNumber(this.couponNumber)
                    .couponCode(CouponCode.of(this.couponCode))
                    .expiredAt(this.expiredAt)
                    .build();
        }

        public void addCouponNumber(String couponNumber) {
            this.couponNumber = couponNumber;
        }

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
