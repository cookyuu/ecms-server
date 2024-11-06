package com.cookyuu.ecms_server.domain.order.repository;

import com.cookyuu.ecms_server.domain.order.dto.OrderDetailDto;
import com.cookyuu.ecms_server.domain.order.dto.SearchOrderDto;
import com.cookyuu.ecms_server.domain.order.entity.*;
import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.entity.SortType;
import com.cookyuu.ecms_server.global.exception.domain.ECMSOrderException;
import com.querydsl.core.types.*;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.cookyuu.ecms_server.domain.member.entity.QMember.member;
import static com.cookyuu.ecms_server.domain.order.entity.QOrder.order;
import static com.cookyuu.ecms_server.domain.order.entity.QOrderLine.orderLine;
import static com.cookyuu.ecms_server.domain.order.entity.SearchOption.*;
import static com.cookyuu.ecms_server.domain.shipment.entity.QShipment.shipment;

@RequiredArgsConstructor
public class OrderCustomRepositoryImpl implements OrderCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SearchOrderDto.Response> searchPageOrderByCreatedAtDesc(SearchOrderDto.Request request) {
        List<SearchOrderDto.Response> content = queryFactory
                .select(Projections.constructor(SearchOrderDto.Response.class,
                        order.id.as("orderId"),
                        order.totalPrice,
                        order.orderNumber,
                        order.status,
                        order.destination,
                        order.destinationDetail,
                        order.recipientName,
                        order.recipientPhoneNumber,
                        member.loginId.as("buyerLoginId"),
                        shipment.shipmentNumber
                        )
                )
                .from(order)
                .leftJoin(order.buyer, member)
                .leftJoin(order.shipment, shipment)
                .where(optionEq(request.getOption(), request.getKeyword()))
                .orderBy(createOrderSpecifier(request.getSortType()))
                .offset(request.getPageable().getOffset())
                .limit(request.getPageable().getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(order.count())
                .from(order)
                .where(optionEq(request.getOption(), request.getKeyword()));

        return PageableExecutionUtils.getPage(content, request.getPageable(), countQuery::fetchOne);
    }

    @Override
    public OrderDetailDto getOrderDetail(String orderNumber) {
        OrderDetailDto.OrderInfo orderInfo = queryFactory
                .select(Projections.constructor(OrderDetailDto.OrderInfo.class,
                        order.orderNumber,
                        order.totalPrice,
                        order.status,
                        order.destination,
                        order.destinationDetail,
                        order.recipientName,
                        order.recipientPhoneNumber,
                        member.id.as("buyerId"),
                        member.loginId.as("buyerLoginId"),
                        member.name.as("buyerName"),
                        shipment.shipmentNumber
                ))
                .from(order)
                .leftJoin(order.buyer, member)
                .leftJoin(order.shipment, shipment)
                .where(orderNumberEq(orderNumber), isNotCanceled())
                .fetchOne();

        List<OrderDetailDto.OrderLineInfo> orderLines = queryFactory
                .select(Projections.constructor(OrderDetailDto.OrderLineInfo.class,
                        orderLine.product.id.as("productId"),
                        orderLine.product.name.as("productName"),
                        orderLine.quantity,
                        orderLine.price,
                        orderLine.product.seller.id.as("sellerId"),
                        orderLine.product.seller.name.as("sellerName")
                ))
                .from(orderLine)
                .join(orderLine.order, order)
                .where(order.orderNumber.eq(orderNumber), isNotCanceled())
                .fetch();

        return OrderDetailDto.builder()
                .orderInfo(orderInfo)
                .orderLines(orderLines)
                .build();
    }

    private BooleanExpression isNotCanceled() {
        return order.isCanceled.eq(false);
    }

    private BooleanExpression optionEq(String option, String keyword) {
        if (StringUtils.isBlank(option)) {
            return null;
        }
        return switch (convertToSearchOption(option)) {
            case ORDER_NUMBER -> order.orderNumber.contains(keyword);
            case LOGIN_ID -> order.buyer.loginId.contains(keyword);
            case STATUS -> order.status.eq(OrderStatus.valueOf(keyword));
            default -> null;
        };
    }

    private BooleanExpression orderNumberEq(String orderNumber) {
        return order.orderNumber.eq(orderNumber);
    }

    private OrderSpecifier createOrderSpecifier(SortType sortType) {
        if (sortType == null) {
            return new OrderSpecifier<>(Order.DESC, order.createdAt);
        }
        return switch(sortType){
            case DESC -> new OrderSpecifier<>(Order.DESC, order.createdAt);
            case ASC -> new OrderSpecifier<>(Order.ASC, order.createdAt);
        };
    }

    private SearchOption convertToSearchOption(String option) {
        if (option.equals(ORDER_NUMBER.getName())){
            return ORDER_NUMBER;
        } else if (option.equals(LOGIN_ID.getName())){
            return LOGIN_ID;
        } else if (option.equals(STATUS.getName())) {
            return STATUS;
        }
        throw new ECMSOrderException(ResultCode.BAD_REQUEST, "[Order::Search] 검색 할 수 없는 옵션입니다. Option : " + option);
    }

}
