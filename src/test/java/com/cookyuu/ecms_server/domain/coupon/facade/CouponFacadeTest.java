package com.cookyuu.ecms_server.domain.coupon.facade;

import com.cookyuu.ecms_server.domain.coupon.dto.CreateCouponDto;
import com.cookyuu.ecms_server.domain.coupon.dto.IssueCouponDto;
import com.cookyuu.ecms_server.domain.coupon.entity.Coupon;
import com.cookyuu.ecms_server.domain.coupon.service.CouponService;
import com.cookyuu.ecms_server.domain.order.dto.CreateOrderDto;
import com.cookyuu.ecms_server.domain.order.dto.CreateOrderItemInfo;
import com.cookyuu.ecms_server.domain.order.dto.OrderShipmentInfo;
import com.cookyuu.ecms_server.domain.order.service.OrderService;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.domain.product.service.ProductService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CouponFacadeTest {
    @Autowired
    private CouponFacade couponFacade;

    @Autowired
    private CouponService couponService;


    @Test
    @DisplayName("선착순 쿠폰 발급 테스트")
    void givenMultiRequest_whenIssueCoupon_thenCheckCouponQuantity() throws InterruptedException {

        // Given
        int numThreads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        Long memberId = 3L;

        CreateCouponDto.Request createCouponInfo = new CreateCouponDto.Request(
                "Test coupon name",
                "2025-01-01 00:00",
                "2025-12-31 00:00",
                "C",
                500,
                null
        );

        CreateCouponDto.Response createCouponRes = couponService.createCoupon(createCouponInfo);

        IssueCouponDto.Request issueCouponInfo = new IssueCouponDto.Request(
                createCouponRes.getCouponNumber()
        );

        // When
        for (int i = 0; i < numThreads; i += 1) {
            executor.submit(() -> {
                try {
                    couponFacade.issueCoupon(memberId, issueCouponInfo);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        // Then
        Coupon coupon = couponService.findCouponByCouponNumber(createCouponRes.getCouponNumber());
        Assertions.assertEquals(coupon.getQuantity(), createCouponInfo.getQuantity() - numThreads);

    }
}