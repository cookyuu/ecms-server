package com.cookyuu.ecms_server.domain.member.service;

import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.repository.MemberRepository;
import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.auth.ValidateEmailException;
import com.cookyuu.ecms_server.global.exception.auth.ValidatePhoneNumberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    @Transactional
    public Member save(Member member) {
        return memberRepository.save(member);
    }

    public void checkDuplicateUserId(String userId) {
        if (memberRepository.existsByUserId(userId)) {
            throw new ValidatePhoneNumberException(ResultCode.VALID_USERID_DUPLICATE);
        }
    }

    public void checkDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new ValidateEmailException(ResultCode.VALID_EMAIL_DUPLICATE);
        }
    }

    public void checkDuplicatePhoneNumber(String phoneNumber) {
        if (memberRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ValidatePhoneNumberException(ResultCode.VALID_PHONENUMBER_DUPLICATE);
        }
    }


}
