package com.cookyuu.ecms_server.domain.auth.service;

import com.cookyuu.ecms_server.domain.auth.dto.SignupDto;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.member.repository.MemberRepository;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
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
    private final MemberService memberService;
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
        memberService.save(member);
    }

    protected String validateAndEncryptPassword(String password) {
        validateUtil.isAvailablePasswordFormat(password);
        return authUtil.encryptPassword(password);
    }

    protected void validateProfileInfo(String userId, String email, String phoneNumber) {
        memberService.checkDuplicateUserId(userId);
        validateUtil.isAvailableUserIdFormat(userId);

        memberService.checkDuplicateEmail(email);
        validateUtil.isAvailableEmailFormat(email);

        memberService.checkDuplicatePhoneNumber(phoneNumber);
        validateUtil.isAvailablePhoneNumberFormat(phoneNumber);
    }





}
