package com.cookyuu.ecms_server.global.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtils {
    private final PasswordEncoder passwordEncoder;

    public String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public void checkPassword(String memberPw, String reqPw) {
        if (!passwordEncoder.matches(reqPw, memberPw)) {
            throw new BadCredentialsException("[checkMemberPw] Password is not matched.");
        }
    }

    public String encPassword(String password) {
        return passwordEncoder.encode(password);
    }
}
