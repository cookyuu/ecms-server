package com.cookyuu.ecms_server.domain.shipment.entity;

import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.domain.BaseTimeEntity;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.domain.shipment.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 배송 엔티티
 *
 * - 주문(Order) 1:1 양방향 관계
 */
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "ecms_shipment",
        indexes = {
                @Index(name = "ecms_shipment_idx_1", columnList = "shipmentNumber", unique = true),
                @Index(name = "ecms_shipment_idx_2", columnList = "orderNumber"),
                @Index(name = "ecms_shipment_idx_3", columnList = "status")
        }
)
public class Shipment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 배송 번호 (고유)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String shipmentNumber;

    /**
     * 현재 위치
     */
    @Column(length = 200)
    private String currentLocation;

    /**
     * 배송 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status;

    /**
     * 도착 일시
     */
    private LocalDateTime arrivedAt;

    /**
     * 주문 번호 (참조용)
     */
    @Column(nullable = false, length = 50)
    private String orderNumber;

    /**
     * 주문 (양방향 관계)
     */
    @OneToOne(mappedBy = "shipment", fetch = FetchType.LAZY)
    private Order order;

    @Builder
     public Shipment (String shipmentNumber, ShipmentStatus status, Order order, String orderNumber) {
         this.shipmentNumber = shipmentNumber;
         this.status = status;
         this.order = order;
         this.orderNumber = orderNumber;
     }

    public void begin(String location) {
        this.currentLocation = location;
        this.status = ShipmentStatus.IN_DELIVERY;
    }

    public void checkStatus(ShipmentStatus status) {
        if (!this.status.equals(status)) {
            throw new BusinessException(ResultCode.SHIPMENT_STATUS_UNMATCHED);
        }
    }

    public void updateLocation(String location) {
        this.currentLocation = location;
    }
}
