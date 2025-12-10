package com.cookyuu.ecms_server.common.utils;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {

    /**
     * UserDetails에서 사용자 ID를 추출합니다.
     *
     * @param user UserDetails 객체
     * @return 사용자 ID (Long)
     */
    public Long getUserId(UserDetails user) {
        return Long.parseLong(user.getUsername());
    }
}
