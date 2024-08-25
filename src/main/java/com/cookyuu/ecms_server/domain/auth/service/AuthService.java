package com.cookyuu.ecms_server.domain.auth.service;

import com.cookyuu.ecms_server.domain.auth.dto.SignupDto;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.repository.MemberRepository;
import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.auth.ValidateEmailException;
import com.cookyuu.ecms_server.global.exception.auth.ValidatePhoneNumberException;
import com.cookyuu.ecms_server.global.utils.AuthUtil;
import com.cookyuu.ecms_server.global.utils.ValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final ValidateUtil validateUtil;
    private final AuthUtil authUtil;

    @Transactional
    public void signupNormal(SignupDto.Request request) {
        String name = request.getName();
        String email = request.getEmail();
        String userId = request.getUserId();
        String phoneNumber = request.getPhoneNumber();
        String password = request.getPassword();
        String address = request.getAddress();

        validateProfileInfo(userId, email, phoneNumber);
        Member member = Member.of(name, email, userId, validateAndEncryptPassword(password), phoneNumber, address);
        memberRepository.save(member);
    }

    private void validateProfileInfo(String userId, String email, String phoneNumber) {
        validateEmail(email);
        validateUserId(userId);
        validatePhoneNumber(phoneNumber);
    }

    private String validateAndEncryptPassword(String password) {
        validateUtil.isAvailablePasswordFormat(password);
        return authUtil.encryptPassword(password);
    }

    public void validateUserId(String userId) {
        if (memberRepository.existsByUserId(userId)) {
            throw new ValidatePhoneNumberException(ResultCode.VALID_USERID_DUPLICATE);
        }
        validateUtil.isAvailableUserIdFormat(userId);
    }

    private void validateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new ValidateEmailException(ResultCode.VALID_EMAIL_DUPLICATE);
        }
        validateUtil.isAvailableEmailFormat(email);
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (memberRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ValidatePhoneNumberException(ResultCode.VALID_PHONENUMBER_DUPLICATE);
        }
        validateUtil.isAvailablePhoneNumberFormat(phoneNumber);
    }



}
