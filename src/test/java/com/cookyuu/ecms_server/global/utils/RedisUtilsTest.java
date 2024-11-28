package com.cookyuu.ecms_server.global.utils;

import com.cookyuu.ecms_server.global.code.RedisKeyCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
class RedisUtilsTest {


    private final RedisUtils redisUtils;

    @Autowired
    RedisUtilsTest (RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    @Test
    void getHashCountData() {
        Map<String, Integer> map = redisUtils.getHashCountDataAndDelete(RedisKeyCode.PRODUCT_HIT_COUNT.getSeparator());
        map.keySet().forEach( a -> System.out.println("key : " + a + "value : " + map.get(a)));
    }
}