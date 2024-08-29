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
            private String userId;
            private String password;
            private String phoneNumber;
            private String address;

            public Request(String name, String email, String userId, String password, String phoneNumber, String address) {
                this.name = name.strip();
                this.email = email.strip();
                this.password = password.strip();
                this.userId = userId.strip();
                this.phoneNumber = phoneNumber.strip();
                this.address = address.strip();
            }
    }
}
