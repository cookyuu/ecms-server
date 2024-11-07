package com.cookyuu.ecms_server.domain.coupon.controller;

import com.cookyuu.ecms_server.domain.coupon.dto.CreateCouponDto;
import com.cookyuu.ecms_server.domain.coupon.dto.IssueCouponDto;
import com.cookyuu.ecms_server.domain.coupon.entity.IssueCoupon;
import com.cookyuu.ecms_server.domain.coupon.facade.CouponFacade;
import com.cookyuu.ecms_server.domain.coupon.service.CouponService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupon")
public class CouponController {
    private final CouponService couponService;
    private final CouponFacade couponFacade;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CreateCouponDto.Response>> createCoupon(@RequestBody CreateCouponDto.Request couponInfo) {
        CreateCouponDto.Response res = couponService.createCoupon(couponInfo);
        return ResponseEntity.ok(ApiResponse.created(res));
    }

    @PostMapping("/issue")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<String>> issueCoupon(@AuthenticationPrincipal UserDetails user, @RequestBody IssueCouponDto.Request couponInfo) {
        couponFacade.issueCoupon(Long.parseLong(user.getUsername()), couponInfo);
        return ResponseEntity.ok(ApiResponse.success("쿠폰 발급 완료"));
    }

}
