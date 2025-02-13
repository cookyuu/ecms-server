package com.cookyuu.ecms_server.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class LoginDto {
    @Getter
    @Builder
    @NoArgsConstructor
    public static class Request {
        private String loginId;
        private String password;

        public Request(String loginId, String password) {
            this.loginId = loginId.strip();
            this.password = password.strip();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String accessToken;
    }
}
