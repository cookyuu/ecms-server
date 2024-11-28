package com.cookyuu.ecms_server.domain.product.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProduct is a Querydsl query type for Product
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProduct extends EntityPathBase<Product> {

    private static final long serialVersionUID = 1149999091L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProduct product = new QProduct("product");

    public final com.cookyuu.ecms_server.global.entity.QBaseTimeEntity _super = new com.cookyuu.ecms_server.global.entity.QBaseTimeEntity(this);

    public final ListPath<com.cookyuu.ecms_server.domain.cart.entity.CartItem, com.cookyuu.ecms_server.domain.cart.entity.QCartItem> cartItems = this.<com.cookyuu.ecms_server.domain.cart.entity.CartItem, com.cookyuu.ecms_server.domain.cart.entity.QCartItem>createList("cartItems", com.cookyuu.ecms_server.domain.cart.entity.CartItem.class, com.cookyuu.ecms_server.domain.cart.entity.QCartItem.class, PathInits.DIRECT2);

    public final QCategory category;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final NumberPath<Integer> hitCount = createNumber("hitCount", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath name = createString("name");

    public final ListPath<com.cookyuu.ecms_server.domain.order.entity.OrderLine, com.cookyuu.ecms_server.domain.order.entity.QOrderLine> orderLines = this.<com.cookyuu.ecms_server.domain.order.entity.OrderLine, com.cookyuu.ecms_server.domain.order.entity.QOrderLine>createList("orderLines", com.cookyuu.ecms_server.domain.order.entity.OrderLine.class, com.cookyuu.ecms_server.domain.order.entity.QOrderLine.class, PathInits.DIRECT2);

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final com.cookyuu.ecms_server.domain.seller.entity.QSeller seller;

    public final NumberPath<Integer> stockQuantity = createNumber("stockQuantity", Integer.class);

    public QProduct(String variable) {
        this(Product.class, forVariable(variable), INITS);
    }

    public QProduct(Path<? extends Product> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProduct(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProduct(PathMetadata metadata, PathInits inits) {
        this(Product.class, metadata, inits);
    }

    public QProduct(Class<? extends Product> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new QCategory(forProperty("category"), inits.get("category")) : null;
        this.seller = inits.isInitialized("seller") ? new com.cookyuu.ecms_server.domain.seller.entity.QSeller(forProperty("seller")) : null;
    }

}

