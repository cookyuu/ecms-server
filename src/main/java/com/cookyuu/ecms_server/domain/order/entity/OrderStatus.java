package com.cookyuu.ecms_server.domain.order.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    ORDER_COMPLETE("ORDER_COMPLETE"), PAYMENT_FAIL("PAYMENT_FAIL"), PAYMENT_COMPLETE("PAYMENT_COMPLETE"),
    PAYMENT_CANCEL("PAYMENT_CANCEL"), SHIPPING("SHIPPING"), COMPLETE("COMPLETE"), CANCELED("CANCELED");

    private String name;

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
