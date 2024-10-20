package com.cookyuu.ecms_server.global.utils;

import com.cookyuu.ecms_server.global.code.CookieCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CookieUtils {
    public Cookie setCookieExpire(CookieCode code, String value, String duration) {
        try {
            Cookie cookie = new Cookie(code.getKey(), value);
            cookie.setMaxAge(Integer.parseInt(duration));
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            return cookie;
        } catch (Exception e) {
            log.error("[SetCookie] Error msg: {}", e.getMessage());
            throw e;
        }
    }

    public void removeCookie(String key, HttpServletResponse response) {
        log.info("[RemoveCookie] key : {}", key);
        Cookie cookie = new Cookie(key, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
