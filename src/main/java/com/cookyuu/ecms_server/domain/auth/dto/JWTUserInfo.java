package com.cookyuu.ecms_server.domain.auth.dto;

import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.entity.RoleType;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JWTUserInfo {
    private Long id;
    private String loginId;
    private String password;
    @Enumerated(EnumType.STRING)
    private RoleType role;

    public void of(Member member) {
        this.id = member.getId();
        this.loginId = member.getLoginId();
        this.password = member.getPassword();
        this.role = member.getRole();
    }
    public void of(Seller seller) {
        this.id = seller.getId();
        this.loginId = seller.getLoginId();
        this.password = seller.getPassword();
        this.role = seller.getRole();
    }
}
