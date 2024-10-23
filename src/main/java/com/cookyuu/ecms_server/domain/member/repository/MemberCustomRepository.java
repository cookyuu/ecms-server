package com.cookyuu.ecms_server.domain.member.repository;

import com.cookyuu.ecms_server.domain.member.dto.MemberDetailDto;

public interface MemberCustomRepository {
    MemberDetailDto getMemberDetail(String loginId);
}
