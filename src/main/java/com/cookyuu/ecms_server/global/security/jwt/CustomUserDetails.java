package com.cookyuu.ecms_server.global.security.jwt;

import com.cookyuu.ecms_server.domain.auth.dto.JWTUserInfo;
import com.cookyuu.ecms_server.domain.member.entity.RoleType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Getter
public class CustomUserDetails implements UserDetails {

    private final JWTUserInfo user;

    public CustomUserDetails (JWTUserInfo user) {
        this.user = user;
    }

    private GrantedAuthority getAuthority(RoleType role) {
        return new SimpleGrantedAuthority("ROLE_" + role.name());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorityList = new ArrayList<>();

        switch (user.getRole()) {
            case ADMIN :
                authorityList.add(getAuthority(RoleType.ADMIN));
                break;
            case SELLER :
                authorityList.add(getAuthority(RoleType.SELLER));
                break;
            case USER :
                authorityList.add(getAuthority(RoleType.USER));
                break;
        }
        return authorityList;
    }

    public RoleType getRole() {
        return user.getRole();
    }
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getId().toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}