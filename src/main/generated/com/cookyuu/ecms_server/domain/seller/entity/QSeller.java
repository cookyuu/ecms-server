package com.cookyuu.ecms_server.domain.seller.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSeller is a Querydsl query type for Seller
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSeller extends EntityPathBase<Seller> {

    private static final long serialVersionUID = -950935765L;

    public static final QSeller seller = new QSeller("seller");

    public final com.cookyuu.ecms_server.global.entity.QBaseTimeEntity _super = new com.cookyuu.ecms_server.global.entity.QBaseTimeEntity(this);

    public final StringPath businessAddress = createString("businessAddress");

    public final StringPath businessContactEmail = createString("businessContactEmail");

    public final StringPath businessContactTelNum = createString("businessContactTelNum");

    public final StringPath businessName = createString("businessName");

    public final StringPath businessNumber = createString("businessNumber");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final StringPath loginId = createString("loginId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final ListPath<com.cookyuu.ecms_server.domain.product.entity.Product, com.cookyuu.ecms_server.domain.product.entity.QProduct> products = this.<com.cookyuu.ecms_server.domain.product.entity.Product, com.cookyuu.ecms_server.domain.product.entity.QProduct>createList("products", com.cookyuu.ecms_server.domain.product.entity.Product.class, com.cookyuu.ecms_server.domain.product.entity.QProduct.class, PathInits.DIRECT2);

    public final EnumPath<com.cookyuu.ecms_server.domain.member.entity.RoleType> role = createEnum("role", com.cookyuu.ecms_server.domain.member.entity.RoleType.class);

    public QSeller(String variable) {
        super(Seller.class, forVariable(variable));
    }

    public QSeller(Path<? extends Seller> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSeller(PathMetadata metadata) {
        super(Seller.class, metadata);
    }

}

