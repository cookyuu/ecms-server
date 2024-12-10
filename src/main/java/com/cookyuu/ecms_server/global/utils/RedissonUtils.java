package com.cookyuu.ecms_server.global.utils;


import com.cookyuu.ecms_server.global.code.RedisKeyCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonUtils {

    private final RedisTemplate redisTemplate;

    public void issueCoupon(String memberId, String couponNumber) {
        redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForValue().decrement(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber);
                operations.opsForSet().add(RedisKeyCode.COUPON_USER_SET_KEY.getSeparator() + couponNumber, memberId);
                operations.opsForZSet().add(
                        RedisKeyCode.COUPON_QUEUE_KEY.getSeparator() + couponNumber,
                        memberId,
                        System.currentTimeMillis()
                );
                return operations.exec();
            }
        });
    }

    public void issueCouponRollback(String memberId, String couponNumber) {
        redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForValue().increment(RedisKeyCode.COUPON_COUNT_KEY.getSeparator() + couponNumber);
                operations.opsForSet().remove(RedisKeyCode.COUPON_USER_SET_KEY.getSeparator() + couponNumber, memberId);
                operations.opsForZSet().remove(RedisKeyCode.COUPON_QUEUE_KEY.getSeparator() + couponNumber, memberId);
                return operations.exec();
            }
        });
    }

    public void recordIssueCouponStatus(String couponNumber, String status) {
        redisTemplate.opsForHash().increment(RedisKeyCode.COUPON_STATUS_KEY.getSeparator() + couponNumber, "success", 1);
    }

    public Boolean isIssuedCoupon(String memberId, String couponNumber) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet()
                .isMember(RedisKeyCode.COUPON_USER_SET_KEY.getSeparator() + couponNumber, memberId));
    }


}
