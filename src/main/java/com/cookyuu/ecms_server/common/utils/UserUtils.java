package com.cookyuu.ecms_server.common.utils;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {

    public Long getUserId(UserDetails user) {
        return Long.parseLong(user.getUsername());
    }
}
