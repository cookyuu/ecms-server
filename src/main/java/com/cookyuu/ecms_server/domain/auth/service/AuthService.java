package com.cookyuu.ecms_server.domain.auth.service;

import com.cookyuu.ecms_server.domain.member.repository.MemberRepository;
import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.auth.ValidateEmailException;
import com.cookyuu.ecms_server.global.exception.auth.ValidatePhoneNumberException;
import com.cookyuu.ecms_server.global.utils.ValidateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final ValidateUtil validateUtil;


    @Transactional(readOnly = true)
    public void validateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new ValidateEmailException(ResultCode.VALID_EMAIL_DUPLICATE);
        }
        validateUtil.isAvailableEmailFormat(email);
    }

    public void validateUserId(String userId) {
        if (memberRepository.existsByUserId(userId)) {
            throw new ValidatePhoneNumberException(ResultCode.VALID_PHONENUMBER_DUPLICATE);
        }
        validateUtil.isAvailableUserIdFormat(userId);
    }

    public void validatePhoneNumber(String phoneNumber) {
        if (memberRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ValidatePhoneNumberException(ResultCode.VALID_PHONENUMBER_DUPLICATE);
        }
        validateUtil.isAvailablePhoneNumberFormat(phoneNumber);
    }

}
