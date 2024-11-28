package com.cookyuu.ecms_server.domain.product.repository;

import com.cookyuu.ecms_server.domain.product.dto.FindProductDetailDto;
import com.cookyuu.ecms_server.domain.product.dto.SearchProductDto;
import com.cookyuu.ecms_server.domain.product.entity.SearchOption;
import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.entity.SortType;
import com.cookyuu.ecms_server.global.exception.domain.ECMSOrderException;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.cookyuu.ecms_server.domain.order.entity.QOrder.order;
import static com.cookyuu.ecms_server.domain.product.entity.QCategory.category;
import static com.cookyuu.ecms_server.domain.product.entity.QProduct.product;
import static com.cookyuu.ecms_server.domain.product.entity.SearchOption.*;
import static com.cookyuu.ecms_server.domain.seller.entity.QSeller.seller;

@RequiredArgsConstructor
public class ProductCustomRepositoryImpl implements ProductCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SearchProductDto.Response> searchPageOrderByCreatedAtDesc(SearchProductDto.Request request) {
        List<SearchProductDto.Response> content = queryFactory
                .select(Projections.constructor(SearchProductDto.Response.class,
                        product.id.as("productId"),
                        product.name.as("productName"),
                        product.description,
                        product.price,
                        product.stockQuantity,
                        product.modifiedAt,
                        product.isDeleted,
                        product.deletedAt,
                        category.id.as("categoryId"),
                        category.name.as("categoryName"),
                        seller.id.as("sellerId"),
                        seller.name.as("sellerName")
                        )
                )
                .from(product)
                .leftJoin(product.seller, seller)
                .leftJoin(product.category, category)
                .where(optionEq(request.getOption(), request.getKeyword()))
                .where(isNotDeleted())
                .orderBy(createOrderSpecifier(request.getSortType()))
                .offset(request.getPageable().getOffset())
                .limit(request.getPageable().getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(order.count())
                .from(order)
                .where(optionEq(request.getOption(), request.getKeyword()))
                .where(isNotDeleted());

        return PageableExecutionUtils.getPage(content, request.getPageable(), countQuery::fetchOne);
    }

    @Override
    public FindProductDetailDto findProductDetail(Long productId) {
        return queryFactory
                .select(Projections.constructor(FindProductDetailDto.class,
                        product.id.as("productId"),
                        product.name.as("productName"),
                        product.description.as("productDescription"),
                        product.price.as("productPrice"),
                        product.stockQuantity.as("productStockQuantity"),
                        product.hitCount.as("productHitCount"),
                        product.isDeleted,
                        category.name.as("categoryName"),
                        seller.id.as("sellerId"),
                        seller.name.as("sellerName")
                        )
                )
                .from(product)
                .leftJoin(product.seller, seller)
                .leftJoin(product.category, category)
                .where(productIdEq(productId))
                .fetchOne();
    }

    private BooleanExpression isNotDeleted() {
        return product.isDeleted.eq(false);
    }

    private BooleanExpression optionEq(String option, String keyword) {
        if (StringUtils.isBlank(option)) {
            return null;
        }
        return switch (convertToSearchOption(option)) {
            case PRODUCT_NAME -> product.name.contains(keyword);
            case CATEGORY_NAME -> product.category.name.contains(keyword);
            case SELLER_NAME ->  product.seller.name.contains(keyword);

            default -> null;
        };
    }

    private OrderSpecifier createOrderSpecifier(SortType sortType) {
        if (sortType == null) {
            return new OrderSpecifier<>(Order.DESC, product.createdAt);
        }
        return switch(sortType){
            case DESC -> new OrderSpecifier<>(Order.DESC, product.createdAt);
            case ASC -> new OrderSpecifier<>(Order.ASC, product.createdAt);
        };
    }

    private SearchOption convertToSearchOption(String option) {
        if (option.equals(PRODUCT_NAME.getName())){
            return PRODUCT_NAME;
        } else if (option.equals(CATEGORY_NAME.getName())){
            return CATEGORY_NAME;
        } else if (option.equals(SELLER_NAME.getName())) {
            return SELLER_NAME;
        }
        throw new ECMSOrderException(ResultCode.BAD_REQUEST, "[Product::Search] 검색 할 수 없는 옵션입니다. Option : " + option);
    }

    private BooleanExpression productIdEq(Long productId) {
        return product.id.eq(productId);
    }

}
