package com.cookyuu.ecms_server.domain.member.service;

import com.cookyuu.ecms_server.domain.auth.dto.JWTUserInfo;
import com.cookyuu.ecms_server.domain.member.dto.MemberDetailDto;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.entity.RoleType;
import com.cookyuu.ecms_server.domain.member.repository.MemberRepository;
import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.auth.UserLoginException;
import com.cookyuu.ecms_server.global.exception.auth.ValidationException;
import com.cookyuu.ecms_server.global.exception.domain.ECMSMemberException;
import com.cookyuu.ecms_server.global.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthUtils authUtils;

    public Member save(Member member) {
        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public MemberDetailDto getMemberDetail(String loginId) {
        return memberRepository.getMemberDetail(loginId);
    }

    @Transactional
    public void updateRole(String role, String loginId) {
        Member member = findMemberByLoginId(loginId);
        RoleType roleType = RoleType.valueOf(role);
        member.updateRole(roleType);
    }

    public JWTUserInfo checkLoginCredentials(String loginId, String password) {
        Member member = (Member) memberRepository.findByLoginId(loginId).orElseThrow(()->
                new UserLoginException(ResultCode.MEMBER_NOT_FOUND));
        log.info("[CheckLoginCredential] Member loginId : {}", member.getLoginId());
        authUtils.checkPassword(member.getPassword(), password);
        JWTUserInfo userInfo = new JWTUserInfo();
        userInfo.of(member);
        return userInfo;
    }

    public void checkDuplicateLoginId(String loginId) {
        if (memberRepository.existsByLoginId(loginId)) {
            throw new ValidationException(ResultCode.VALID_LOGINID_DUPLICATE);
        }
    }

    public void checkDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new ValidationException(ResultCode.VALID_EMAIL_DUPLICATE);
        }
    }
    public void checkDuplicatePhoneNumber(String phoneNumber) {
        if (memberRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ValidationException(ResultCode.VALID_PHONENUMBER_DUPLICATE);
        }
    }

    public Member findMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new ECMSMemberException(ResultCode.MEMBER_NOT_FOUND));
    }

    public Member findMemberByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId).orElseThrow(() -> new ECMSMemberException(ResultCode.MEMBER_NOT_FOUND));
    }
}
