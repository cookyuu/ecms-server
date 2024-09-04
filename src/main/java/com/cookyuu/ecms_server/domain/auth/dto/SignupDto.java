package com.cookyuu.ecms_server.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SignupDto {
    @Getter
    @Builder
    @NoArgsConstructor
    public static class Request {
            private String name;
            private String email;
            private String loginId;
            private String password;
            private String phoneNumber;
            private String address;

            public Request(String name, String email, String loginId, String password, String phoneNumber, String address) {
                this.name = name.strip();
                this.email = email.strip();
                this.password = password.strip();
                this.loginId = loginId.strip();
                this.phoneNumber = phoneNumber.strip();
                this.address = address.strip();
            }
    }
}
