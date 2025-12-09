package com.cookyuu.ecms_server.domain.member.logging;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Slf4j
@Component
public class MemberLogHelper {

    /**
     * 회원 등록 성공 Info 로그
     */
    public void logMemberRegistered(Long memberId, String loginId, String email, String role, long durationMs) {
        log.atInfo()
            .addKeyValue(EVENT, MEMBER_REGISTERED)
            .addKeyValue(MEMBER_ID, memberId)
            .addKeyValue(LOGIN_ID, loginId)
            .addKeyValue(EMAIL, email)
            .addKeyValue(USER_ROLE, role)
            .addKeyValue(DURATION_MS, durationMs)
            .log("Member registered successfully");
    }

    /**
     * 회원 등록 실패 Error 로그
     */
    public void logMemberRegistrationFailed(String loginId, String errorMessage, long durationMs, Exception e) {
        log.atError()
            .addKeyValue(EVENT, BUSINESS_ERROR)
            .addKeyValue(LOGIN_ID, loginId)
            .addKeyValue(ERROR_MESSAGE, errorMessage)
            .addKeyValue(DURATION_MS, durationMs)
            .setCause(e)
            .log("Member registration failed");
    }

    /**
     * 회원 역할 변경 Info 로그
     */
    public void logMemberRoleChanged(Long memberId, String loginId, String oldRole, String newRole, long durationMs) {
        log.atInfo()
            .addKeyValue(EVENT, MEMBER_ROLE_CHANGED)
            .addKeyValue(MEMBER_ID, memberId)
            .addKeyValue(LOGIN_ID, loginId)
            .addKeyValue("old_role", oldRole)
            .addKeyValue("new_role", newRole)
            .addKeyValue(DURATION_MS, durationMs)
            .log("Member role updated successfully");
    }

    /**
     * 로그인 성공 Info 로그
     */
    public void logLoginSuccess(Long memberId, String loginId, String role, long durationMs) {
        log.atInfo()
            .addKeyValue(EVENT, LOGIN_SUCCESS)
            .addKeyValue(MEMBER_ID, memberId)
            .addKeyValue(LOGIN_ID, loginId)
            .addKeyValue(USER_ROLE, role)
            .addKeyValue(DURATION_MS, durationMs)
            .log("Login successful");
    }

    /**
     * 로그인 실패 - 회원을 찾을 수 없음 Warn 로그
     */
    public void logLoginFailedMemberNotFound(String loginId, ResultCode resultCode, long durationMs) {
        log.atWarn()
            .addKeyValue(EVENT, LOGIN_FAILED)
            .addKeyValue(LOGIN_ID, loginId)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .addKeyValue(ERROR_MESSAGE, "Member not found")
            .addKeyValue(DURATION_MS, durationMs)
            .log("Login failed - member not found");
    }

    /**
     * 로그인 실패 - 잘못된 비밀번호 Warn 로그
     */
    public void logLoginFailedInvalidPassword(String loginId, ResultCode resultCode, long durationMs) {
        log.atWarn()
            .addKeyValue(EVENT, LOGIN_FAILED)
            .addKeyValue(LOGIN_ID, loginId)
            .addKeyValue(ERROR_CODE, resultCode.getCode())
            .addKeyValue(ERROR_MESSAGE, "Invalid password")
            .addKeyValue(DURATION_MS, durationMs)
            .log("Login failed - invalid credentials");
    }
}
