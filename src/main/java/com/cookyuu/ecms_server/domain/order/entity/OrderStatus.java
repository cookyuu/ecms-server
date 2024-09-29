package com.cookyuu.ecms_server.domain.order.entity;

import java.util.Arrays;
import java.util.List;

public enum OrderStatus {
    ORDER_COMPLETE, PAYMENT_WAIT ,PAYMENT_COMPLETE, SHIPPING, COMPLETE, CANCEL_WAIT, CANCELED;

    public static boolean isPossibleOrderCancel(OrderStatus status) {
        OrderStatus[] statuses = {OrderStatus.ORDER_COMPLETE, OrderStatus.PAYMENT_WAIT, OrderStatus.PAYMENT_COMPLETE};
        List<OrderStatus> cancelPossibleStatusList = Arrays.asList(statuses);
        return cancelPossibleStatusList.contains(status);
    }
}
