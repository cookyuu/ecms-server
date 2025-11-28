package com.cookyuu.ecms_server.domain.coupon.controller;

import com.cookyuu.ecms_server.domain.coupon.dto.CreateCouponDto;
import com.cookyuu.ecms_server.domain.coupon.facade.CouponFacade;
import com.cookyuu.ecms_server.domain.coupon.service.CouponService;
import com.cookyuu.ecms_server.common.web.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupon")
public class CouponController {
    private final CouponService couponService;
    private final CouponFacade couponFacade;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateCouponDto.Response>> createCoupon(@RequestBody CreateCouponDto.Request couponInfo) {
        CreateCouponDto.Response res = couponService.createCoupon(couponInfo);
        return ResponseEntity.ok(ApiResponse.created(res));
    }

    @PostMapping("/issue")
    public ResponseEntity<ApiResponse<String>> issueCoupon(@AuthenticationPrincipal UserDetails user, @RequestParam(name = "couponNumber") String couponNumber) throws Exception {
        couponFacade.issueCoupon(Long.parseLong(user.getUsername()), couponNumber);
        return ResponseEntity.ok(ApiResponse.success("쿠폰 발급 완료"));
    }
}
