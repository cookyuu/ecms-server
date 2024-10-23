package com.cookyuu.ecms_server.domain.member.repository;

import com.cookyuu.ecms_server.domain.member.dto.MemberDetailDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.cookyuu.ecms_server.domain.member.entity.QMember.member;

@RequiredArgsConstructor
public class MemberSearchRepositoryImpl implements MemberSearchRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public MemberDetailDto getMemberDetail(Long id) {
       return (MemberDetailDto) queryFactory.select(Projections.constructor(MemberDetailDto.class,
                member.id,
                member.name,
                member.email,
                member.loginId,
                member.phoneNumber,
                member.address,
                member.role
        )).from(member).where(memberIdEq(id)).fetchJoin().fetch();
    }

    private BooleanExpression memberIdEq(Long id) {
        return member.id.eq(id);
    }
}
