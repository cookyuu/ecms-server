package com.cookyuu.ecms_server.domain.order.service;

import com.cookyuu.ecms_server.domain.order.dto.CreateOrderItemInfo;
import com.cookyuu.ecms_server.domain.order.dto.ReviseOrderItemInfo;
import com.cookyuu.ecms_server.domain.order.entity.OrderLine;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Slf4j
@Component
public class OrderStockManager {

    /**
     * 주문 생성 시 상품 재고 차감
     *
     * @param orderItems 주문할 상품 목록
     */
    public void decreaseStockForOrder(List<CreateOrderItemInfo> orderItems) {
        for (CreateOrderItemInfo orderItem : orderItems) {
            Product product = orderItem.getProduct();
            int quantity = orderItem.getQuantity();

            product.subQuantity(quantity);

            log.atDebug()
                .addKeyValue("operation", "decreaseStock")
                .addKeyValue(PRODUCT_ID, product.getId())
                .addKeyValue(QUANTITY, quantity)
                .addKeyValue(STOCK_QUANTITY, product.getStockQuantity())
                .log("Stock decreased for order creation");
        }
    }

    /**
     * 주문 취소 시 상품 재고 복구
     *
     * @param orderLines 취소할 주문 라인 목록
     */
    public void restoreStockForCancel(List<OrderLine> orderLines) {
        for (OrderLine orderLine : orderLines) {
            Product product = orderLine.getProduct();
            int quantity = orderLine.getQuantity();

            product.addQuantity(quantity);

            log.atDebug()
                .addKeyValue("operation", "restoreStock")
                .addKeyValue(PRODUCT_ID, product.getId())
                .addKeyValue(QUANTITY, quantity)
                .addKeyValue(STOCK_QUANTITY, product.getStockQuantity())
                .log("Stock restored for order cancellation");
        }
    }

    /**
     * 주문 수정 시 기존 주문의 상품 재고 복구
     *
     * @param orderLines 기존 주문 라인 목록
     */
    public void restoreStockForRevision(List<OrderLine> orderLines) {
        for (OrderLine orderLine : orderLines) {
            Product product = orderLine.getProduct();
            int quantity = orderLine.getQuantity();

            product.addQuantity(quantity);

            log.atDebug()
                .addKeyValue(EVENT, ORDER_REVISED)
                .addKeyValue(PRODUCT_ID, product.getId())
                .addKeyValue(QUANTITY, quantity)
                .addKeyValue(STOCK_QUANTITY, product.getStockQuantity())
                .log("Restored product quantity for order revision");
        }
    }

    /**
     * 주문 수정 시 새로운 주문의 상품 재고 차감
     *
     * @param orderItems 새로운 주문 상품 목록
     */
    public void decreaseStockForRevision(List<ReviseOrderItemInfo> orderItems) {
        for (ReviseOrderItemInfo orderItem : orderItems) {
            Product product = orderItem.getProduct();
            int quantity = orderItem.getQuantity();

            product.subQuantity(quantity);

            log.atDebug()
                .addKeyValue(EVENT, ORDER_REVISED)
                .addKeyValue(PRODUCT_ID, product.getId())
                .addKeyValue(QUANTITY, quantity)
                .addKeyValue(STOCK_QUANTITY, product.getStockQuantity())
                .log("Stock decreased for order revision");
        }
    }
}
