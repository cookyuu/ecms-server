package com.cookyuu.ecms_server.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SignupDto {
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
            private String name;
            private String email;
            private String userId;
            private String password;
            private String phoneNumber;
            private String address;
    }
}
