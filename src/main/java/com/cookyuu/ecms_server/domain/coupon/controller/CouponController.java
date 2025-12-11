package com.cookyuu.ecms_server.domain.coupon.controller;

import com.cookyuu.ecms_server.domain.coupon.dto.CreateCouponDto;
import com.cookyuu.ecms_server.domain.coupon.facade.CouponFacade;
import com.cookyuu.ecms_server.domain.coupon.service.CouponService;
import com.cookyuu.ecms_server.common.utils.UserUtils;
import com.cookyuu.ecms_server.common.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "쿠폰 API", description = "쿠폰 생성 및 발급 API. Redis 분산락을 통한 동시성 제어가 적용됩니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupon")
public class CouponController {
    private final CouponService couponService;
    private final CouponFacade couponFacade;
    private final UserUtils userUtils;

    @Operation(summary = "쿠폰 생성", description = "새로운 쿠폰을 생성합니다. 관리자 권한이 필요합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "쿠폰 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CreateCouponDto.Response>> createCoupon(@RequestBody CreateCouponDto.Request couponInfo) {
        CreateCouponDto.Response res = couponService.createCoupon(couponInfo);
        return ResponseEntity.ok(ApiResponse.created(res));
    }

    @Operation(
        summary = "쿠폰 발급",
        description = "선착순 쿠폰을 발급받습니다. Redis 분산락을 통해 동시성 문제를 해결하고 재고를 관리합니다. " +
                     "발급 실패 시 롤백이 진행됩니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "쿠폰 발급 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "쿠폰을 찾을 수 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "쿠폰 재고 소진 또는 이미 발급받은 쿠폰")
    })
    @PostMapping("/issue")
    public ResponseEntity<ApiResponse<String>> issueCoupon(
        @AuthenticationPrincipal UserDetails user,
        @Parameter(description = "발급받을 쿠폰 번호", required = true)
        @RequestParam(name = "couponNumber") String couponNumber) throws Exception {
        couponFacade.issueCoupon(userUtils.getUserId(user), couponNumber);
        return ResponseEntity.ok(ApiResponse.success("쿠폰 발급 완료"));
    }
}
