package com.cookyuu.ecms_server.domain.shipment.repository;

import com.cookyuu.ecms_server.domain.shipment.dto.ShipmentDetailDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.cookyuu.ecms_server.domain.shipment.entity.QShipment.shipment;

@RequiredArgsConstructor
public class ShipmentCustomRepositoryImpl implements ShipmentCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public ShipmentDetailDto getShipmentDetail(String shipmentNumber) {
        return queryFactory
                .select(Projections.constructor(ShipmentDetailDto.class,
                        shipment.id.as("shipmentId"),
                        shipment.shipmentNumber,
                        shipment.currentLocation,
                        shipment.status,
                        shipment.arrivedAt,
                        shipment.orderNumber
                        )
                )
                .from(shipment)
                .where(shipmentNumberEq(shipmentNumber))
                .fetchFirst();
    }

    private BooleanExpression shipmentNumberEq(String shipmentNumber) {
        return shipment.shipmentNumber.eq(shipmentNumber);
    }
}
