package com.cookyuu.ecms_server.domain.auth.controller;

import com.cookyuu.ecms_server.domain.auth.dto.LoginDto;
import com.cookyuu.ecms_server.domain.auth.dto.SignupDto;
import com.cookyuu.ecms_server.domain.auth.service.AuthService;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import com.cookyuu.ecms_server.global.code.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<Object>> signupNormal(@RequestBody SignupDto.Request request) {
        authService.signupNormal(request);
        return ResponseEntity.ok(ApiResponse.created(ResultCode.SIGNUP_SUCCESS));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginDto.Response>> loginNormal(@RequestBody LoginDto.Request request, HttpServletResponse response) {
        LoginDto.Response res = authService.loginNormal(request, response);
        return ResponseEntity.ok(ApiResponse.success(ResultCode.NORMAL_LOGIN_SUCCESS, res));
    }

    @PostMapping("/login/seller")
    public ResponseEntity<ApiResponse<LoginDto.Response>> loginSeller(@RequestBody LoginDto.Request request, HttpServletResponse response) {
        LoginDto.Response res = authService.loginSeller(request, response);
        return ResponseEntity.ok(ApiResponse.success(ResultCode.SELLER_LOGIN_SUCCESS, res));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logoutNormal(@AuthenticationPrincipal UserDetails user, HttpServletRequest request, HttpServletResponse response) {
        authService.logoutNormal(user, request, response);
        return ResponseEntity.ok(ApiResponse.success(ResultCode.LOGOUT_SUCCESS));
    }

    @GetMapping("/validation/login-id")
    public ResponseEntity<ApiResponse<Object>> validateUserId(@RequestParam(name = "loginId") String loginId) {
        memberService.checkDuplicateLoginId(loginId);
        return ResponseEntity.ok(ApiResponse.success(ResultCode.VALID_USERID_SUCCESS));
    }
}
