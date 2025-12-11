package com.cookyuu.ecms_server.domain.member.controller;

import com.cookyuu.ecms_server.domain.member.dto.MemberDetailDto;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.common.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 API", description = "회원 정보 조회 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "회원 상세 정보 조회", description = "로그인 ID로 회원의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/{loginId}")
    public ResponseEntity<ApiResponse<MemberDetailDto>> getMemberDetail(
        @Parameter(description = "조회할 회원의 로그인 ID", required = true)
        @PathVariable(name = "loginId") String loginId) {
        MemberDetailDto res = memberService.getMemberDetail(loginId);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @Operation(summary = "회원 권한 변경", description = "회원의 권한(Role)을 변경합니다. 관리자 전용 API입니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @PutMapping("/role")
    public ResponseEntity<ApiResponse<String>> updateMemberRole(
        @Parameter(description = "변경할 권한 (USER, SELLER, ADMIN)", required = true)
        @RequestParam(name = "role") String role,
        @Parameter(description = "대상 회원의 로그인 ID", required = true)
        @RequestParam(name = "loginId") String loginId) {
        memberService.updateRole(role, loginId);
        return ResponseEntity.ok(ApiResponse.success("성공"));
    }

}
