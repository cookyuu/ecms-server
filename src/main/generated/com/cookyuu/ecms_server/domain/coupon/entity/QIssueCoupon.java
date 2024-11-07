package com.cookyuu.ecms_server.domain.coupon.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QIssueCoupon is a Querydsl query type for IssueCoupon
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QIssueCoupon extends EntityPathBase<IssueCoupon> {

    private static final long serialVersionUID = 364007980L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QIssueCoupon issueCoupon = new QIssueCoupon("issueCoupon");

    public final com.cookyuu.ecms_server.global.entity.QBaseTimeEntity _super = new com.cookyuu.ecms_server.global.entity.QBaseTimeEntity(this);

    public final QCoupon coupon;

    public final DateTimePath<java.time.LocalDateTime> couponUsedAt = createDateTime("couponUsedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> expiredAt = createDateTime("expiredAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isUseAble = createBoolean("isUseAble");

    public final com.cookyuu.ecms_server.domain.member.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public QIssueCoupon(String variable) {
        this(IssueCoupon.class, forVariable(variable), INITS);
    }

    public QIssueCoupon(Path<? extends IssueCoupon> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QIssueCoupon(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QIssueCoupon(PathMetadata metadata, PathInits inits) {
        this(IssueCoupon.class, metadata, inits);
    }

    public QIssueCoupon(Class<? extends IssueCoupon> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.coupon = inits.isInitialized("coupon") ? new QCoupon(forProperty("coupon")) : null;
        this.member = inits.isInitialized("member") ? new com.cookyuu.ecms_server.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

