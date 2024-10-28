package com.cookyuu.ecms_server.domain.seller.controller;

import com.cookyuu.ecms_server.domain.seller.dto.DeleteSellerDto;
import com.cookyuu.ecms_server.domain.seller.dto.RegisterSellerDto;
import com.cookyuu.ecms_server.domain.seller.dto.SellerDetailDto;
import com.cookyuu.ecms_server.domain.seller.dto.UpdateSellerDto;
import com.cookyuu.ecms_server.domain.seller.service.SellerService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seller")
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<RegisterSellerDto.Response>> registerSeller(@Valid @RequestBody RegisterSellerDto.Request sellerInfo) {
        RegisterSellerDto.Response res = sellerService.registerSeller(sellerInfo);
        return ResponseEntity.ok(ApiResponse.created(res));
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PutMapping("/info")
    public ResponseEntity<ApiResponse<Object>> updateSellerInfo(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody UpdateSellerDto.Request sellerInfo) {
        sellerService.updateSellerInfo(user, sellerInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @DeleteMapping()
    public ResponseEntity<ApiResponse<Object>> deleteSeller(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody DeleteSellerDto.Request sellerInfo) {
        sellerService.deleteSeller(user, sellerInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping
    public ResponseEntity<ApiResponse<SellerDetailDto>> getSellerDetail(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(sellerService.getSellerDetail(Long.parseLong(user.getUsername()))));
    }
}
