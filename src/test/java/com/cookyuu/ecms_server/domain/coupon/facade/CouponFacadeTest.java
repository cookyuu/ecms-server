package com.cookyuu.ecms_server.domain.coupon.facade;

import com.cookyuu.ecms_server.domain.coupon.service.CouponService;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CouponFacadeTest {
    @Autowired
    private CouponFacade couponFacade;

    @Autowired
    private CouponService couponService;

/*
    @Test
    @DisplayName("선착순 쿠폰 발급 테스트")
    void givenMultiRequest_whenIssueCoupon_thenCheckCouponQuantity() throws InterruptedException {

        // Given
        int numThreads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        long memberId = 2L;

        CreateCouponDto.Request createCouponInfo = new CreateCouponDto.Request(
                "Test coupon name",
                "2025-01-01 00:00",
                "2025-12-31 00:00",
                "C",
                500,
                null
        );

        CreateCouponDto.Response createCouponRes = couponService.createCoupon(createCouponInfo);

        String couponNumber = createCouponRes.getCouponNumber();

        // When
        for (int i = 0; i < numThreads; i += 1) {
            executor.submit(() -> {
                try {
                    couponFacade.issueCoupon(memberId, couponNumber);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        // Then
        Coupon coupon = couponService.findCouponByCouponNumber(couponNumber);
        Assertions.assertEquals(coupon.getQuantity(), createCouponInfo.getQuantity() - numThreads);

    }
 */
}