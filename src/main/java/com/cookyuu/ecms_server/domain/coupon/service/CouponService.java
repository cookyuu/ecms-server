package com.cookyuu.ecms_server.domain.coupon.service;

import com.cookyuu.ecms_server.domain.coupon.dto.CreateCouponDto;
import com.cookyuu.ecms_server.domain.coupon.entity.Coupon;
import com.cookyuu.ecms_server.domain.coupon.entity.CouponCode;
import com.cookyuu.ecms_server.domain.coupon.repository.CouponRepository;
import com.cookyuu.ecms_server.global.code.RedisKeyCode;
import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.domain.ECMSCouponException;
import com.cookyuu.ecms_server.global.utils.RedisUtils;
import com.cookyuu.ecms_server.global.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final RedisTemplate redisTemplate;
    private final RedisUtils redisUtils;

    @Transactional
    public CreateCouponDto.Response createCoupon(CreateCouponDto.Request couponInfo) {
        String couponNumber = makeCouponNumber(CouponCode.of(couponInfo.getCouponCode()));
        log.debug("[Coupon:Create] Make coupon number. OK!, coupon number : {}", couponNumber);
        Coupon coupon = Coupon.builder()
                .name(couponInfo.getName())
                .startAt(StringUtils.parseToLocalDateTime(couponInfo.getStartAt()))
                .expiredAt(StringUtils.parseToLocalDateTime(couponInfo.getExpiredAt()))
                .couponCode(CouponCode.of(couponInfo.getCouponCode()))
                .couponNumber(couponNumber)
                .quantity(couponInfo.getQuantity())
                .discountPrice(isFixPriceCoupon(couponInfo.getCouponCode()) ? isNullDiscountPrice(couponInfo.getDiscountPrice()) : null)
                .build();
        couponRepository.save(coupon);
        redisUtils.setData(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber, String.valueOf(couponInfo.getQuantity()));

        log.debug("[Coupon:Create] Insert Coupon OK!");
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

    @Transactional
    public Coupon findCouponByCouponNumber(String couponNumber) {
        return couponRepository.findByCouponNumber(couponNumber).orElseThrow(ECMSCouponException::new);
    }

    public void validateCoupon(String couponNumber) {
        Coupon coupon = findCouponByCouponNumber(couponNumber);
        if (coupon.isExpired()) {
            log.info("[Coupon::Validate] Coupon is expired. couponNumber : {}", couponNumber);
            throw new ECMSCouponException(ResultCode.COUPON_UNUSABLE, "만료된 쿠폰입니다. ");
        }
        if (coupon.getQuantity() == 0) {
            log.info("[Coupon::Validate] Coupon is sold out, couponNumber : {}", couponNumber);
            throw new ECMSCouponException(ResultCode.COUPON_SOLD_OUT);
        }
    }

    @Transactional
    public void save(Coupon coupon) {
        couponRepository.save(coupon);
    }
}
