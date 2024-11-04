package com.cookyuu.ecms_server.domain.product.repository;

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
                        product.name,
                        product.description,
                        product.price,
                        product.stockQuantity,
                        product.modifiedAt,
                        product.isDeleted,
                        product.deletedAt,
                        category.id.as("categoryId"),
                        category.name.as("categoryName"),
                        seller.loginId.as("sellerId"),
                        seller.name.as("sellerName")
                        )
                )
                .from(product)
                .leftJoin(product.category, category)
                .leftJoin(product.seller, seller)
                .where(optionEq(request.getOption(), request.getKeyword()))
                .orderBy(createProductSpecifier(request.getSortType()))
                .offset(request.getPageable().getOffset())
                .limit(request.getPageable().getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(optionEq(request.getOption(), request.getKeyword()));

        return PageableExecutionUtils.getPage(content, request.getPageable(), countQuery::fetchOne);
    }


    private BooleanExpression isNotCanceled() {
        return product.isDeleted.eq(false);
    }

    private BooleanExpression optionEq(String option, String keyword) {
        if (StringUtils.isBlank(option)) {
            return null;
        }
        return switch (convertToSearchOption(option)) {
            case PRODUCT_NAME -> product.name.contains(keyword);
            case SELLER_NAME -> product.seller.name.contains(keyword);
            case CATEGORY_NAME -> product.category.name.eq(keyword);
            default -> null;
        };
    }

    private BooleanExpression productNameEq(String productName) {
        return product.name.eq(productName);
    }

    private OrderSpecifier createProductSpecifier(SortType sortType) {
        if (sortType == null) {
            return new OrderSpecifier<>(Order.DESC, product.modifiedAt);
        }
        return switch(sortType){
            case DESC -> new OrderSpecifier<>(Order.DESC, product.modifiedAt);
            case ASC -> new OrderSpecifier<>(Order.ASC, product.modifiedAt);
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

}
