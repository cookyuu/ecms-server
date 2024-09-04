package com.cookyuu.ecms_server.domain.auth.dto;

import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.entity.RoleType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JWTUserInfo {
    private Long memberId;
    private String loginId;
    private String password;
    @Enumerated(EnumType.STRING)
    private RoleType role;

    public JWTUserInfo of(Member member) {
        return JWTUserInfo.builder()
                .memberId(member.getId())
                .loginId(member.getLoginId())
                .password(member.getPassword())
                .role(member.getRole())
                .build();
    }
}
