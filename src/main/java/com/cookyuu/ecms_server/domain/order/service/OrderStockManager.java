package com.cookyuu.ecms_server.domain.order.service;

import com.cookyuu.ecms_server.domain.order.dto.CreateOrderItemInfo;
import com.cookyuu.ecms_server.domain.order.dto.ReviseOrderItemInfo;
import com.cookyuu.ecms_server.domain.order.entity.OrderLine;
import com.cookyuu.ecms_server.domain.order.logging.OrderLogHelper;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderStockManager {

    private final OrderLogHelper orderLogHelper;

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
            orderLogHelper.logStockDecrease("decreaseStockForOrder", product.getId(),
                quantity, product.getStockQuantity());
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
            orderLogHelper.logStockRestore("restoreStockForCancel", product.getId(),
                quantity, product.getStockQuantity());
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
            orderLogHelper.logStockRestoreForRevision(product.getId(), quantity, product.getStockQuantity());
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
            orderLogHelper.logStockDecreaseForRevision(product.getId(), quantity, product.getStockQuantity());
        }
    }
}
