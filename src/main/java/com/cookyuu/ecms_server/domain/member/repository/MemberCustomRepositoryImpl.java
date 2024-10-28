package com.cookyuu.ecms_server.domain.member.repository;

import com.cookyuu.ecms_server.domain.member.dto.MemberDetailDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.cookyuu.ecms_server.domain.member.entity.QMember.member;

@RequiredArgsConstructor
public class MemberCustomRepositoryImpl implements MemberCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public MemberDetailDto getMemberDetail(String loginId) {
       return queryFactory.select(Projections.constructor(MemberDetailDto.class,
                member.id,
                member.name,
                member.email,
                member.loginId,
                member.phoneNumber,
                member.address,
                member.role
        )).from(member).where(memberLoginIdEq(loginId)).fetch().get(0);
    }

    private BooleanExpression memberLoginIdEq(String loginId) {
        return member.loginId.eq(loginId);
    }
}
