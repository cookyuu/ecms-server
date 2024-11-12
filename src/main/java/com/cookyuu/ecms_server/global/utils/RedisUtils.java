package com.cookyuu.ecms_server.global.utils;

import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.utils.EcmsRedisException;
import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final StringRedisTemplate redisTemplate;

    // key를 통해 Value 리턴
    public String getData(String key) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            return valueOperations.get(key);
        } catch (RedisException e) {
            log.error("[GetRedisData] ", e);
            throw new EcmsRedisException(ResultCode.REDIS_COMMON_EXP);
        }
    }

    // 유효시간 동안 key,value 저장
    public void setDataExpire(String key, String value, long duration) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            Duration expiredDuration = Duration.ofSeconds(duration);
            valueOperations.set(key, value, expiredDuration);
        } catch (RedisException e) {
            log.error("[SaveRedisData] ", e);
            throw new EcmsRedisException(ResultCode.REDIS_COMMON_EXP);
        }
    }

    public void setData(String key, String value) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            valueOperations.set(key, value);
        } catch (RedisException e) {
            log.error("[SaveRedisData] ", e);
            throw new EcmsRedisException(ResultCode.REDIS_COMMON_EXP);
        }
    }

    // 삭제
    public void deleteData(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RedisException e) {
            log.error("[DeleteRedisData] ", e);
            throw new EcmsRedisException(ResultCode.REDIS_COMMON_EXP);
        }
    }
}
