package com.cookyuu.ecms_server.domain.member.controller;

import com.cookyuu.ecms_server.domain.member.dto.MemberDetailDto;
import com.cookyuu.ecms_server.domain.member.service.MemberService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberDetailDto>> getMemberDetail(@PathVariable(name = "id") Long memberId) {
        MemberDetailDto res = memberService.getMemberDetail(memberId);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

}
