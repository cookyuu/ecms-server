package com.cookyuu.ecms_server.domain.coupon.service;

import com.cookyuu.ecms_server.domain.coupon.dto.CreateCouponDto;
import com.cookyuu.ecms_server.domain.coupon.entity.Coupon;
import com.cookyuu.ecms_server.domain.coupon.entity.CouponCode;
import com.cookyuu.ecms_server.domain.coupon.repository.CouponRepository;
import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.domain.ECMSCouponException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    @Transactional
    public CreateCouponDto.Response createCoupon(CreateCouponDto.Request couponInfo) {
        String couponNumber = makeCouponNumber(CouponCode.valueOf(couponInfo.getCouponCode()));
        Coupon coupon = Coupon.builder()
                .name(couponInfo.getName())
                .expiredAt(couponInfo.getExpiredAt())
                .couponCode(CouponCode.valueOf(couponInfo.getCouponCode()))
                .discountPrice(isFixPriceCoupon(couponInfo.getCouponCode()) ? isNullDiscountPrice(couponInfo.getDiscountPrice()) : null)
                .build();
        couponRepository.save(coupon);
        return CreateCouponDto.Response.builder()
                .couponNumber(couponNumber)
                .build();
    }

    private boolean isFixPriceCoupon(String couponCode) {
        return couponCode.equals("M");
    }

    private Integer isNullDiscountPrice(Integer price) {
        if (price == null || price == 0) {
            throw new ECMSCouponException(ResultCode.COUPON_PRICE_EMPTY);
        }
        return price;
    }

    private String makeCouponNumber(CouponCode couponCode) {
        StringBuilder sb = new StringBuilder();
        String formatDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
        sb.append("CP").append(couponCode.getCode()).append(formatDate);
        for (int i = 0; i < 5; i++) {
            int random = (int) (Math.random() * 10);
            sb.append(random);
        }
        return sb.toString();
    }
}