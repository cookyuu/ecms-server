package com.cookyuu.ecms_server.domain.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = -315012575L;

    public static final QMember member = new QMember("member1");

    public final com.cookyuu.ecms_server.global.entity.QBaseTimeEntity _super = new com.cookyuu.ecms_server.global.entity.QBaseTimeEntity(this);

    public final StringPath address = createString("address");

    public final ListPath<com.cookyuu.ecms_server.domain.cart.entity.Cart, com.cookyuu.ecms_server.domain.cart.entity.QCart> carts = this.<com.cookyuu.ecms_server.domain.cart.entity.Cart, com.cookyuu.ecms_server.domain.cart.entity.QCart>createList("carts", com.cookyuu.ecms_server.domain.cart.entity.Cart.class, com.cookyuu.ecms_server.domain.cart.entity.QCart.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath loginId = createString("loginId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath name = createString("name");

    public final ListPath<com.cookyuu.ecms_server.domain.order.entity.Order, com.cookyuu.ecms_server.domain.order.entity.QOrder> orders = this.<com.cookyuu.ecms_server.domain.order.entity.Order, com.cookyuu.ecms_server.domain.order.entity.QOrder>createList("orders", com.cookyuu.ecms_server.domain.order.entity.Order.class, com.cookyuu.ecms_server.domain.order.entity.QOrder.class, PathInits.DIRECT2);

    public final StringPath password = createString("password");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final EnumPath<RoleType> role = createEnum("role", RoleType.class);

    public QMember(String variable) {
        super(Member.class, forVariable(variable));
    }

    public QMember(Path<? extends Member> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMember(PathMetadata metadata) {
        super(Member.class, metadata);
    }

}

