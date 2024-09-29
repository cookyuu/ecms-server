package com.cookyuu.ecms_server.domain.order.mapper;

import com.cookyuu.ecms_server.domain.order.dto.ReviseOrderItemInfo;
import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.domain.order.entity.OrderLine;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReviseOrderLineMapper {
    public static List<OrderLine> toEntityList(List<ReviseOrderItemInfo> orderItemList, Order order) {
        List<OrderLine> orderLines = new ArrayList<>();
        orderItemList.forEach(orderItemInfo -> {
            orderLines.add(OrderLine.builder()
                    .quantity(orderItemInfo.getQuantity())
                    .price(orderItemInfo.getPrice())
                    .order(order)
                    .product(orderItemInfo.getProduct())
                    .build());
        });
        return orderLines;
    }
}
