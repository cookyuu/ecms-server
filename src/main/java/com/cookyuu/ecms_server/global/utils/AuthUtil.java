package com.cookyuu.ecms_server.global.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtil {
    private final PasswordEncoder passwordEncoder;

    public String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }
}
