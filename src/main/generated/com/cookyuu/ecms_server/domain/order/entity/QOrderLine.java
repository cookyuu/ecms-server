package com.cookyuu.ecms_server.domain.order.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrderLine is a Querydsl query type for OrderLine
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderLine extends EntityPathBase<OrderLine> {

    private static final long serialVersionUID = 1012036071L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrderLine orderLine = new QOrderLine("orderLine");

    public final com.cookyuu.ecms_server.global.entity.QBaseTimeEntity _super = new com.cookyuu.ecms_server.global.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final QOrder order;

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final com.cookyuu.ecms_server.domain.product.entity.QProduct product;

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public QOrderLine(String variable) {
        this(OrderLine.class, forVariable(variable), INITS);
    }

    public QOrderLine(Path<? extends OrderLine> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrderLine(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrderLine(PathMetadata metadata, PathInits inits) {
        this(OrderLine.class, metadata, inits);
    }

    public QOrderLine(Class<? extends OrderLine> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.order = inits.isInitialized("order") ? new QOrder(forProperty("order"), inits.get("order")) : null;
        this.product = inits.isInitialized("product") ? new com.cookyuu.ecms_server.domain.product.entity.QProduct(forProperty("product"), inits.get("product")) : null;
    }

}

