package com.cookyuu.ecms_server.domain.order.mapper;

import com.cookyuu.ecms_server.domain.order.dto.CreateOrderItemInfo;
import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.domain.order.entity.OrderLine;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CreateOrderLineMapper {
    public static List<OrderLine> toEntityList(List<CreateOrderItemInfo> orderItemList, Order order) {
        List<OrderLine> orderLines = new ArrayList<>();
        orderItemList.forEach(orderItemInfo -> {
            orderLines.add(OrderLine.createSnapshot(
                    order,
                    orderItemInfo.getProduct(),
                    orderItemInfo.getQuantity()
            ));
        });
        return orderLines;
    }
}
