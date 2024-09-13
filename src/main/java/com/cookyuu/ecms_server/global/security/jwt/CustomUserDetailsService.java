package com.cookyuu.ecms_server.global.security.jwt;

import com.cookyuu.ecms_server.domain.auth.dto.JWTUserInfo;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.entity.RoleType;
import com.cookyuu.ecms_server.domain.member.repository.MemberRepository;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import com.cookyuu.ecms_server.domain.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final SellerRepository sellerRepository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Member member = memberRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new UsernameNotFoundException("[ValidateJwtToken] Can not find member. No exists"));
        JWTUserInfo userInfo = new JWTUserInfo();
        userInfo.of(member);
        return new CustomUserDetails(userInfo);
    }

    public UserDetails loadUserByUsername(String id, RoleType role) {
        JWTUserInfo userInfo = new JWTUserInfo();
        if (role.equals(RoleType.USER)) {
            Member member = memberRepository.findById(Long.parseLong(id))
                    .orElseThrow(() -> new UsernameNotFoundException("[ValidateJwtToken] Can not find member. No exists"));
            userInfo.of(member);
        } else if (role.equals(RoleType.SELLER)) {
            Seller seller = sellerRepository.findById(Long.parseLong(id))
                    .orElseThrow(() -> new UsernameNotFoundException("[ValidateJwtToken] Can not find seller. No exists"));
            userInfo.of(seller);
        }
        return new CustomUserDetails(userInfo);
    }
}