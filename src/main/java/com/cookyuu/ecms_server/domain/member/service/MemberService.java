package com.cookyuu.ecms_server.domain.member.service;

import com.cookyuu.ecms_server.domain.auth.dto.JWTUserInfo;
import com.cookyuu.ecms_server.domain.member.dto.MemberDetailDto;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import com.cookyuu.ecms_server.domain.member.repository.MemberRepository;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.AuthenticationException;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthUtils authUtils;

    @Transactional
    public Member save(Member member) {
        long startTime = System.currentTimeMillis();

        try {
            Member savedMember = memberRepository.save(member);

            log.atInfo()
                .addKeyValue(EVENT, MEMBER_REGISTERED)
                .addKeyValue(MEMBER_ID, savedMember.getId())
                .addKeyValue(LOGIN_ID, savedMember.getLoginId())
                .addKeyValue(EMAIL, savedMember.getEmail())
                .addKeyValue(USER_ROLE, savedMember.getRole().name())
                .addKeyValue(DURATION_MS, System.currentTimeMillis() - startTime)
                .log("Member registered successfully");

            return savedMember;
        } catch (Exception e) {
            log.atError()
                .addKeyValue(EVENT, BUSINESS_ERROR)
                .addKeyValue(LOGIN_ID, member.getLoginId())
                .addKeyValue(ERROR_MESSAGE, e.getMessage())
                .addKeyValue(DURATION_MS, System.currentTimeMillis() - startTime)
                .setCause(e)
                .log("Member registration failed");
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public MemberDetailDto getMemberDetail(String loginId) {
        return memberRepository.getMemberDetail(loginId);
    }

    @Transactional
    public void updateRole(String role, String loginId) {
        long startTime = System.currentTimeMillis();

        Member member = findMemberByLoginId(loginId);
        RoleType oldRole = member.getRole();
        RoleType roleType = RoleType.valueOf(role);
        member.updateRole(roleType);

        log.atInfo()
            .addKeyValue(EVENT, MEMBER_ROLE_CHANGED)
            .addKeyValue(MEMBER_ID, member.getId())
            .addKeyValue(LOGIN_ID, loginId)
            .addKeyValue("old_role", oldRole.name())
            .addKeyValue("new_role", roleType.name())
            .addKeyValue(DURATION_MS, System.currentTimeMillis() - startTime)
            .log("Member role updated successfully");
    }

    public JWTUserInfo checkLoginCredentials(String loginId, String password) {
        long startTime = System.currentTimeMillis();

        try {
            Member member = memberRepository.findByLoginId(loginId).orElseThrow(() -> {
                log.atWarn()
                    .addKeyValue(EVENT, LOGIN_FAILED)
                    .addKeyValue(LOGIN_ID, loginId)
                    .addKeyValue(ERROR_CODE, ResultCode.MEMBER_NOT_FOUND.getCode())
                    .addKeyValue(ERROR_MESSAGE, "Member not found")
                    .addKeyValue(DURATION_MS, System.currentTimeMillis() - startTime)
                    .log("Login failed - member not found");
                return new AuthenticationException(ResultCode.MEMBER_NOT_FOUND);
            });

            authUtils.checkPassword(member.getPassword(), password);

            log.atInfo()
                .addKeyValue(EVENT, LOGIN_SUCCESS)
                .addKeyValue(MEMBER_ID, member.getId())
                .addKeyValue(LOGIN_ID, member.getLoginId())
                .addKeyValue(USER_ROLE, member.getRole().name())
                .addKeyValue(DURATION_MS, System.currentTimeMillis() - startTime)
                .log("Login successful");

            JWTUserInfo userInfo = new JWTUserInfo();
            userInfo.of(member);
            return userInfo;

        } catch (AuthenticationException e) {
            if (!e.getResultCode().equals(ResultCode.MEMBER_NOT_FOUND)) {
                log.atWarn()
                    .addKeyValue(EVENT, LOGIN_FAILED)
                    .addKeyValue(LOGIN_ID, loginId)
                    .addKeyValue(ERROR_CODE, e.getResultCode().getCode())
                    .addKeyValue(ERROR_MESSAGE, "Invalid password")
                    .addKeyValue(DURATION_MS, System.currentTimeMillis() - startTime)
                    .log("Login failed - invalid credentials");
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
