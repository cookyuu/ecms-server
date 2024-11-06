package com.cookyuu.ecms_server.domain.member.controller;

import com.cookyuu.ecms_server.domain.member.dto.MemberDetailDto;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/{loginId}")
    public ResponseEntity<ApiResponse<MemberDetailDto>> getMemberDetail(@PathVariable(name = "loginId") String loginId) {
        MemberDetailDto res = memberService.getMemberDetail(loginId);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PreAuthorize("permitAll()")
    @PutMapping("/role")
    public ResponseEntity<ApiResponse<String>> updateMemberRole(@RequestParam(name = "role") String role, @RequestParam(name = "loginId") String loginId) {
        memberService.updateRole(role, loginId);
        return ResponseEntity.ok(ApiResponse.success("성공"));
    }

}
