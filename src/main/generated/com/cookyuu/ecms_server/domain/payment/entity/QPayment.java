package com.cookyuu.ecms_server.domain.payment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPayment is a Querydsl query type for Payment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayment extends EntityPathBase<Payment> {

    private static final long serialVersionUID = -948967661L;

    public static final QPayment payment = new QPayment("payment");

    public final com.cookyuu.ecms_server.global.entity.QBaseTimeEntity _super = new com.cookyuu.ecms_server.global.entity.QBaseTimeEntity(this);

    public final NumberPath<Integer> amount = createNumber("amount", Integer.class);

    public final NumberPath<Long> buyerId = createNumber("buyerId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> canceledAt = createDateTime("canceledAt", java.time.LocalDateTime.class);

    public final StringPath cancelReason = createString("cancelReason");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath orderNumber = createString("orderNumber");

    public final StringPath paymentFailMsg = createString("paymentFailMsg");

    public final EnumPath<PaymentMethod> paymentMethod = createEnum("paymentMethod", PaymentMethod.class);

    public final StringPath paymentNumber = createString("paymentNumber");

    public final NumberPath<Long> sellerId = createNumber("sellerId", Long.class);

    public final EnumPath<PaymentStatus> status = createEnum("status", PaymentStatus.class);

    public QPayment(String variable) {
        super(Payment.class, forVariable(variable));
    }

    public QPayment(Path<? extends Payment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPayment(PathMetadata metadata) {
        super(Payment.class, metadata);
    }

}

