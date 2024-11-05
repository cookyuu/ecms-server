package com.cookyuu.ecms_server.domain.coupon.entity;

import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.domain.ECMSCouponException;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CouponCode {
    NO_COOPON("N", 0),
    FIVE_PERCENT("A", 5),
    SEVEN_PERCENT("B", 7),
    TEN_PERCENT("C", 10),
    FIFTEEN_PERCENT("D", 15),
    TWENTY_PERCENT("E", 20),
    THIRTY_PERCENT("F", 30),
    FORTY_PERCENT("G", 40),
    HALF_PERCENT("O", 50),
    SIXTY_PERCENT("P", 60),
    SEVENTY_PERCENT("Q", 70),
    EIGHTY_PERCENT("R", 80),
    NINETY_PERCENT("S", 90),
    FIX_DISCOUNT_PRICE("T", 0);
    private String code;
    private Integer discountPercent;

    public static CouponCode of(String couponCode) {
        for (CouponCode coupon : CouponCode.values()) {
            if (coupon.code.equals(couponCode)) {
                return coupon;
            }
        }
        throw new ECMSCouponException(ResultCode.COUPON_CODE_UNMATCHED);
    }
}
