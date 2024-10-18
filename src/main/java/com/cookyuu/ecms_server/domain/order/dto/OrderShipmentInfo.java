package com.cookyuu.ecms_server.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderShipmentInfo {
    private String destination;
    private String destinationDetail;
    private String recipientName;
    private String recipientPhoneNumber;
}
