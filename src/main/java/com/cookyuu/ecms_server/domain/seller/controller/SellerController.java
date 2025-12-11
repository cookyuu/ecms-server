package com.cookyuu.ecms_server.domain.seller.controller;

import com.cookyuu.ecms_server.domain.seller.dto.DeleteSellerDto;
import com.cookyuu.ecms_server.domain.seller.dto.RegisterSellerDto;
import com.cookyuu.ecms_server.domain.seller.dto.SellerDetailDto;
import com.cookyuu.ecms_server.domain.seller.dto.UpdateSellerDto;
import com.cookyuu.ecms_server.domain.seller.service.SellerService;
import com.cookyuu.ecms_server.common.utils.UserUtils;
import com.cookyuu.ecms_server.common.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "판매자 API", description = "판매자 등록, 조회, 수정, 삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seller")
public class SellerController {

    private final SellerService sellerService;
    private final UserUtils userUtils;

    @Operation(summary = "판매자 등록", description = "새로운 판매자를 등록합니다. 사업자 정보를 포함해야 합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (유효성 검증 실패)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 등록된 판매자")
    })
    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<RegisterSellerDto.Response>> registerSeller(@Valid @RequestBody RegisterSellerDto.Request sellerInfo) {
        RegisterSellerDto.Response res = sellerService.registerSeller(sellerInfo);
        return ResponseEntity.ok(ApiResponse.created(res));
    }

    @Operation(summary = "판매자 정보 수정", description = "판매자의 정보를 수정합니다. 본인만 수정할 수 있습니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "판매자를 찾을 수 없음")
    })
    @PutMapping("/info")
    public ResponseEntity<ApiResponse<Object>> updateSellerInfo(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody UpdateSellerDto.Request sellerInfo) {
        sellerService.updateSellerInfo(user, sellerInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "판매자 삭제", description = "판매자 정보를 삭제합니다. 본인만 삭제할 수 있습니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "판매자를 찾을 수 없음")
    })
    @DeleteMapping()
    public ResponseEntity<ApiResponse<Object>> deleteSeller(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody DeleteSellerDto.Request sellerInfo) {
        sellerService.deleteSeller(user, sellerInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "판매자 상세 조회", description = "현재 로그인한 판매자의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "판매자를 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<SellerDetailDto>> getSellerDetail(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(sellerService.getSellerDetail(userUtils.getUserId(user))));
    }
}
