package com.cookyuu.ecms_server.domain.order.entity;

import java.util.Arrays;
import java.util.List;

public enum OrderStatus {
    ORDER_COMPLETE, PAYMENT_FAIL, PAYMENT_COMPLETE, PAYMENT_CANCEL, SHIPPING, COMPLETE, CANCELED;

    public static boolean isPossibleOrderCancel(OrderStatus status) {
        OrderStatus[] statuses = {ORDER_COMPLETE, PAYMENT_FAIL, PAYMENT_COMPLETE};
        List<OrderStatus> cancelPossibleStatusList = Arrays.asList(statuses);
        return cancelPossibleStatusList.contains(status);
    }

    public static boolean isPossibleOrderRevise(OrderStatus status) {
        OrderStatus[] statuses = {ORDER_COMPLETE, PAYMENT_FAIL};
        List<OrderStatus> revisePossibleStatusList = Arrays.asList(statuses);
        return revisePossibleStatusList.contains(status);
    }
}
