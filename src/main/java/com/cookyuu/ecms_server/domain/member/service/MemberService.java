package com.cookyuu.ecms_server.domain.member.service;

import com.cookyuu.ecms_server.domain.auth.dto.JWTUserInfo;
import com.cookyuu.ecms_server.domain.member.dto.MemberDetailDto;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import com.cookyuu.ecms_server.domain.member.logging.MemberLogHelper;
import com.cookyuu.ecms_server.domain.member.repository.MemberRepository;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.AuthenticationException;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthUtils authUtils;
    private final MemberLogHelper memberLogHelper;

    @Transactional
    public Member save(Member member) {
        long startTime = System.currentTimeMillis();

        try {
            Member savedMember = memberRepository.save(member);

            memberLogHelper.logMemberRegistered(savedMember.getId(), savedMember.getLoginId(),
                savedMember.getEmail(), savedMember.getRole().name(), System.currentTimeMillis() - startTime);

            return savedMember;
        } catch (Exception e) {
            memberLogHelper.logMemberRegistrationFailed(member.getLoginId(), e.getMessage(),
                System.currentTimeMillis() - startTime, e);
            throw e;
        }
    }

    @Cacheable(value = "memberDetail", key = "#loginId")
    @Transactional(readOnly = true)
    public MemberDetailDto getMemberDetail(String loginId) {
        return memberRepository.getMemberDetail(loginId);
    }

    @CacheEvict(value = "memberDetail", key = "#loginId")
    @Transactional
    public void updateRole(String role, String loginId) {
        long startTime = System.currentTimeMillis();

        Member member = findMemberByLoginId(loginId);
        RoleType oldRole = member.getRole();
        RoleType roleType = RoleType.valueOf(role);
        member.updateRole(roleType);

        memberLogHelper.logMemberRoleChanged(member.getId(), loginId, oldRole.name(),
            roleType.name(), System.currentTimeMillis() - startTime);
    }

    public JWTUserInfo checkLoginCredentials(String loginId, String password) {
        long startTime = System.currentTimeMillis();

        try {
            Member member = memberRepository.findByLoginId(loginId).orElseThrow(() -> {
                memberLogHelper.logLoginFailedMemberNotFound(loginId, ResultCode.MEMBER_NOT_FOUND,
                    System.currentTimeMillis() - startTime);
                return new AuthenticationException(ResultCode.MEMBER_NOT_FOUND);
            });

            authUtils.checkPassword(member.getPassword(), password);

            memberLogHelper.logLoginSuccess(member.getId(), member.getLoginId(),
                member.getRole().name(), System.currentTimeMillis() - startTime);

            JWTUserInfo userInfo = new JWTUserInfo();
            userInfo.of(member);
            return userInfo;

        } catch (AuthenticationException e) {
            if (!e.getResultCode().equals(ResultCode.MEMBER_NOT_FOUND)) {
                memberLogHelper.logLoginFailedInvalidPassword(loginId, e.getResultCode(),
                    System.currentTimeMillis() - startTime);
            }
            throw e;
        }
    }

    public void checkDuplicateLoginId(String loginId) {
        if (memberRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ResultCode.VALID_LOGINID_DUPLICATE);
        }
    }

    public void checkDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ResultCode.VALID_EMAIL_DUPLICATE);
        }
    }
    public void checkDuplicatePhoneNumber(String phoneNumber) {
        if (memberRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BusinessException(ResultCode.VALID_PHONENUMBER_DUPLICATE);
        }
    }

    public Member findMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new BusinessException(ResultCode.MEMBER_NOT_FOUND));
    }

    public Member findMemberByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId).orElseThrow(() -> new BusinessException(ResultCode.MEMBER_NOT_FOUND));
    }
}
