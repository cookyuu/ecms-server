package com.cookyuu.ecms_server.domain.auth.controller;

import com.cookyuu.ecms_server.domain.auth.dto.LoginDto;
import com.cookyuu.ecms_server.domain.auth.dto.SignupDto;
import com.cookyuu.ecms_server.domain.auth.service.AuthService;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.common.web.dto.ApiResponse;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증 API", description = "회원가입, 로그인, 로그아웃 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

    @Operation(summary = "일반 회원 가입", description = "새로운 일반 회원을 등록합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 사용자")
    })
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<Object>> signupNormal(@RequestBody SignupDto.Request request) {
        authService.signupNormal(request);
        return ResponseEntity.ok(ApiResponse.created(ResultCode.SIGNUP_SUCCESS));
    }

    @Operation(summary = "일반 회원 로그인", description = "일반 회원 계정으로 로그인합니다. JWT 토큰이 쿠키로 발급됩니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginDto.Response>> loginNormal(@RequestBody LoginDto.Request request, HttpServletResponse response) {
        LoginDto.Response res = authService.loginNormal(request, response);
        return ResponseEntity.ok(ApiResponse.success(ResultCode.NORMAL_LOGIN_SUCCESS, res));
    }

    @Operation(summary = "판매자 로그인", description = "판매자 계정으로 로그인합니다. JWT 토큰이 쿠키로 발급됩니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "판매자 권한이 없음")
    })
    @PostMapping("/login/seller")
    public ResponseEntity<ApiResponse<LoginDto.Response>> loginSeller(@RequestBody LoginDto.Request request, HttpServletResponse response) {
        LoginDto.Response res = authService.loginSeller(request, response);
        return ResponseEntity.ok(ApiResponse.success(ResultCode.SELLER_LOGIN_SUCCESS, res));
    }

    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃합니다. JWT 토큰이 무효화됩니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logoutNormal(@AuthenticationPrincipal UserDetails user, HttpServletRequest request, HttpServletResponse response) {
        authService.logoutNormal(user, request, response);
        return ResponseEntity.ok(ApiResponse.success(ResultCode.LOGOUT_SUCCESS));
    }

    @Operation(summary = "로그인 ID 중복 확인", description = "회원가입 시 로그인 ID 중복 여부를 확인합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용 가능한 ID"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 사용 중인 ID")
    })
    @GetMapping("/validation/login-id")
    public ResponseEntity<ApiResponse<Object>> validateUserId(
        @Parameter(description = "확인할 로그인 ID", required = true)
        @RequestParam(name = "loginId") String loginId) {
        memberService.checkDuplicateLoginId(loginId);
        return ResponseEntity.ok(ApiResponse.success(ResultCode.VALID_USERID_SUCCESS));
    }
}
