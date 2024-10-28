package com.cookyuu.ecms_server.domain.seller.repository;

import com.cookyuu.ecms_server.domain.seller.dto.SellerDetailDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.cookyuu.ecms_server.domain.seller.entity.QSeller.seller;

@RequiredArgsConstructor
public class SellerCustomRepositoryImpl implements SellerCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public SellerDetailDto getSellerDetail(Long id) {
        return queryFactory
                .select(Projections.constructor(SellerDetailDto.class,
                        seller.id.as("sellerId"),
                        seller.name,
                        seller.businessName,
                        seller.businessNumber,
                        seller.businessAddress,
                        seller.businessContactTelNum,
                        seller.businessContactEmail,
                        seller.loginId
                        ))
                .from(seller)
                .where(sellerIdEq(id))
                .fetchFirst();
    }

    private BooleanExpression sellerIdEq(Long id) {
        return seller.id.eq(id);
    }
}
