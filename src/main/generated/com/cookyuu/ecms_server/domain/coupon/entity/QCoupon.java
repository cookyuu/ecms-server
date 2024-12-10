package com.cookyuu.ecms_server.domain.coupon.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;

/**
 * QCoupon is a Querydsl query type for Coupon
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCoupon extends EntityPathBase<Coupon> {

    private static final long serialVersionUID = -819118023L;

    public static final QCoupon coupon = new QCoupon("coupon");

    public final com.cookyuu.ecms_server.global.entity.QBaseTimeEntity _super = new com.cookyuu.ecms_server.global.entity.QBaseTimeEntity(this);

    public final EnumPath<CouponCode> couponCode = createEnum("couponCode", CouponCode.class);

    public final StringPath couponNumber = createString("couponNumber");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DatePath<java.time.LocalDate> expiredAt = createDate("expiredAt", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> discountPrice = createNumber("discountPrice", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> expiredAt = createDateTime("expiredAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isExpired = createBoolean("isExpired");

    public final ListPath<IssueCoupon, QIssueCoupon> issueCoupons = this.<IssueCoupon, QIssueCoupon>createList("issueCoupons", IssueCoupon.class, QIssueCoupon.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath name = createString("name");
    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final NumberPath<Long> stockQuantity = createNumber("stockQuantity", Long.class);
    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> startAt = createDateTime("startAt", java.time.LocalDateTime.class);

    public QCoupon(String variable) {
        super(Coupon.class, forVariable(variable));
    }

    public QCoupon(Path<? extends Coupon> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCoupon(PathMetadata metadata) {
        super(Coupon.class, metadata);
    }

}

