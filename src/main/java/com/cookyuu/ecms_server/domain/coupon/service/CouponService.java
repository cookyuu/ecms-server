package com.cookyuu.ecms_server.domain.coupon.service;

import com.cookyuu.ecms_server.domain.coupon.dto.CreateCouponDto;
import com.cookyuu.ecms_server.domain.coupon.entity.Coupon;
import com.cookyuu.ecms_server.domain.coupon.enums.CouponCode;
import com.cookyuu.ecms_server.domain.coupon.repository.CouponRepository;
import com.cookyuu.ecms_server.common.enums.RedisKeyCode;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.generator.BusinessNumberGenerator;
import com.cookyuu.ecms_server.common.utils.RedisUtils;
import com.cookyuu.ecms_server.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final RedisTemplate redisTemplate;
    private final RedisUtils redisUtils;
    private final BusinessNumberGenerator businessNumberGenerator;

    @Transactional
    public CreateCouponDto.Response createCoupon(CreateCouponDto.Request couponInfo) {
        String couponNumber = businessNumberGenerator.generateCouponNumber(CouponCode.of(couponInfo.getCouponCode()));
        log.atDebug()
                .addKeyValue(COUPON_NUMBER, couponNumber)
                .addKeyValue(COUPON_CODE, couponInfo.getCouponCode())
                .log("Coupon number generated");
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

        log.atInfo()
                .addKeyValue(EVENT, COUPON_CREATED)
                .addKeyValue(COUPON_NUMBER, couponNumber)
                .addKeyValue(COUPON_CODE, couponInfo.getCouponCode())
                .addKeyValue(QUANTITY, couponInfo.getQuantity())
                .addKeyValue(DISCOUNT_PRICE, couponInfo.getDiscountPrice())
                .log("Coupon created successfully");
        return CreateCouponDto.Response.builder()
                .couponNumber(couponNumber)
                .build();
    }

    private boolean isFixPriceCoupon(String couponCode) {
        return couponCode.equals("M");
    }

    private Integer isNullDiscountPrice(Integer price) {
        if (price == null || price == 0) {
            throw new BusinessException(ResultCode.COUPON_PRICE_EMPTY);
        }
        return price;
    }

    @Transactional
    public Coupon findCouponByCouponNumber(String couponNumber) {
        return couponRepository.findByCouponNumber(couponNumber).orElseThrow(() -> new BusinessException(ResultCode.COUPON_NOT_FOUND));
    }

    public void validateCoupon(String couponNumber) {
        Coupon coupon = findCouponByCouponNumber(couponNumber);
        if (coupon.isExpired()) {
            log.atWarn()
                    .addKeyValue(EVENT, COUPON_EXPIRED)
                    .addKeyValue(COUPON_NUMBER, couponNumber)
                    .addKeyValue(COUPON_ID, coupon.getId())
                    .log("Coupon is expired");
            throw new BusinessException(ResultCode.COUPON_UNUSABLE, "만료된 쿠폰입니다. ");
        }
        if (coupon.getQuantity() == 0) {
            log.atWarn()
                    .addKeyValue(EVENT, VALIDATION_ERROR)
                    .addKeyValue(COUPON_NUMBER, couponNumber)
                    .addKeyValue(COUPON_ID, coupon.getId())
                    .addKeyValue(QUANTITY, 0)
                    .log("Coupon is sold out");
            throw new BusinessException(ResultCode.COUPON_SOLD_OUT);
        }
    }

    @Transactional
    public void save(Coupon coupon) {
        couponRepository.save(coupon);
    }
}
