package com.cookyuu.ecms_server.domain.member.dto;

import com.cookyuu.ecms_server.domain.member.entity.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDetailDto {
    private Long id;
    private String name;
    private String email;
    private String loginId;
    private String phoneNumber;
    private String address;
    private RoleType role;
}
