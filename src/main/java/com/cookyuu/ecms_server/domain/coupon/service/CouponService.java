package com.cookyuu.ecms_server.domain.coupon.service;

import com.cookyuu.ecms_server.domain.coupon.dto.CreateCouponDto;
import com.cookyuu.ecms_server.domain.coupon.entity.CouponCode;
import com.cookyuu.ecms_server.domain.coupon.repository.CouponRepository;
import com.cookyuu.ecms_server.domain.order.entity.OrderCode;
import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.domain.ECMSCouponException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    @Transactional
    public CreateCouponDto.Response createCoupon(CreateCouponDto.Request couponInfo) {
        CouponCode code = CouponCode.of(couponInfo.getCouponCode());
        if (code.equals(CouponCode.FIX_DISCOUNT_PRICE) && couponInfo.getPrice() == null) {
            throw new ECMSCouponException(ResultCode.COUPON_EMPTY_PRICE);
        }

        String couponNumber = createCouponNumber(code);
        couponInfo.addCouponNumber(couponNumber);
        couponRepository.save(couponInfo.toEntity());
        return CreateCouponDto.Response.builder().couponNumber(couponNumber).build();
    }

    private String createCouponNumber(CouponCode code) {
        StringBuilder sb = new StringBuilder();
        String formatDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
        sb.append("CP").append(code.getCode()).append(formatDate);
        for (int i = 0; i < 5; i++) {
            int random = (int) (Math.random() * 10);
            sb.append(random);
        }
        return sb.toString();
    }
}

