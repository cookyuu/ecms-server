package com.cookyuu.ecms_server.domain.product.scheduler;


import com.cookyuu.ecms_server.domain.product.service.ProductService;
import com.cookyuu.ecms_server.common.enums.RedisKeyCode;
import com.cookyuu.ecms_server.common.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductHitCountScheduler {
    private final RedisUtils redisUtils;
    private final ProductService productService;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void executor() {
        Map<String, Integer> productHitsMap = redisUtils.getHashCountDataAndDelete(RedisKeyCode.PRODUCT_HIT_COUNT.getSeparator());
        if (productHitsMap.isEmpty()) {
            return;
        }
        productHitsMap.keySet().forEach(key -> {
            int hitCount = productHitsMap.get(key);
            Long productId = Long.valueOf(key.substring(RedisKeyCode.PRODUCT_HIT_COUNT.getSeparator().length()));
            productService.applyHitCount(productId, hitCount);
        });
    }
}
