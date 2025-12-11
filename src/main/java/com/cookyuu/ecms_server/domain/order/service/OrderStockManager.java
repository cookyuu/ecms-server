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

    public void decreaseStockForOrder(List<CreateOrderItemInfo> orderItems) {
        for (CreateOrderItemInfo orderItem : orderItems) {
            Product product = orderItem.getProduct();
            int quantity = orderItem.getQuantity();

            product.subQuantity(quantity);
            orderLogHelper.logStockDecrease("decreaseStockForOrder", product.getId(),
                quantity, product.getStockQuantity());
        }
    }

    public void restoreStockForCancel(List<OrderLine> orderLines) {
        for (OrderLine orderLine : orderLines) {
            Product product = orderLine.getProduct();
            int quantity = orderLine.getQuantity();

            product.addQuantity(quantity);
            orderLogHelper.logStockRestore("restoreStockForCancel", product.getId(),
                quantity, product.getStockQuantity());
        }
    }

    public void restoreStockForRevision(List<OrderLine> orderLines) {
        for (OrderLine orderLine : orderLines) {
            Product product = orderLine.getProduct();
            int quantity = orderLine.getQuantity();

            product.addQuantity(quantity);
            orderLogHelper.logStockRestoreForRevision(product.getId(), quantity, product.getStockQuantity());
        }
    }

    public void decreaseStockForRevision(List<ReviseOrderItemInfo> orderItems) {
        for (ReviseOrderItemInfo orderItem : orderItems) {
            Product product = orderItem.getProduct();
            int quantity = orderItem.getQuantity();

            product.subQuantity(quantity);
            orderLogHelper.logStockDecreaseForRevision(product.getId(), quantity, product.getStockQuantity());
        }
    }
}
