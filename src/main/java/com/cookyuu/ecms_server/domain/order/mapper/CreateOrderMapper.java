package com.cookyuu.ecms_server.domain.order.mapper;

import com.cookyuu.ecms_server.domain.order.dto.CreateOrderDto;
import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.domain.order.entity.OrderLine;
import com.cookyuu.ecms_server.domain.order.entity.OrderStatus;

import java.util.ArrayList;
import java.util.List;

public class CreateOrderMapper {
    public static CreateOrderDto.Response toDto(Order order) {
         return CreateOrderDto.Response.builder()
                 .orderNumber(order.getOrderNumber())
                 .build();
    }

    public static Order toEntity(CreateOrderDto.Request orderInfo) {
        return Order.builder()
                .totalPrice(orderInfo.getTotalPrice())
                .orderNumber(orderInfo.getOrderNumber())
                .status(OrderStatus.ORDER_COMPLETE)
                .buyer(orderInfo.getBuyer())
                .build();
    }
}