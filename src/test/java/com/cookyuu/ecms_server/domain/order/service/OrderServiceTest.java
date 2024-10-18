package com.cookyuu.ecms_server.domain.order.service;

import com.cookyuu.ecms_server.domain.order.dto.CreateOrderDto;
import com.cookyuu.ecms_server.domain.order.dto.CreateOrderItemInfo;
import com.cookyuu.ecms_server.domain.order.dto.OrderShipmentInfo;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.domain.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("주문 동시성 테스트")
    void givenThousandRequest_whenCreateOrder_thenCheckConcurrency() throws InterruptedException {
        // Given
        int numThreads = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        Long userId = 1L;
        List<CreateOrderItemInfo> reqItemList = new ArrayList<>();
        CreateOrderItemInfo reqItem = CreateOrderItemInfo.builder()
                .productId(2L)
                .price(10000)
                .quantity(1)
                .build();
        reqItemList.add(reqItem);

        OrderShipmentInfo shipInfo = OrderShipmentInfo.builder()
                .destination("테스트 주소")
                .destinationDetail("테스트 상세 주소")
                .recipientName("수신자")
                .recipientPhoneNumber("010-1123-1231")
                .build();
        CreateOrderDto.Request request = CreateOrderDto.Request.builder()
                .orderItemList(reqItemList)
                .shipmentInfo(shipInfo)
                .build();
        // When
        for (int i = 0; i < numThreads; i += 1) {
            executor.submit(() -> {
                try {
                    orderService.createOrder(userId, request);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Then
        Product product = productService.findProductById(2L);
        assertThat(product.getStockQuantity().equals(0));
    }
}