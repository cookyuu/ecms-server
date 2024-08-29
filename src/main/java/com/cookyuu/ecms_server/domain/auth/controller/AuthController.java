package com.cookyuu.ecms_server.domain.auth.controller;

import com.cookyuu.ecms_server.domain.auth.dto.SignupDto;
import com.cookyuu.ecms_server.domain.auth.service.AuthService;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import com.cookyuu.ecms_server.global.dto.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        return ResponseEntity.ok(ApiResponse.created());
    }

    @GetMapping("/validation/user-id")
    public ResponseEntity<ApiResponse<Object>> validateUserId(@RequestParam(name = "userId") String userId) {
        memberService.checkDuplicateUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(ResultCode.VALID_USERID_SUCCESS));
    }
}
