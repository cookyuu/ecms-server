package com.cookyuu.ecms_server.domain.seller.controller;

import com.cookyuu.ecms_server.domain.seller.dto.RegisterSellerDto;
import com.cookyuu.ecms_server.domain.seller.dto.UpdateSellerDto;
import com.cookyuu.ecms_server.domain.seller.service.SellerService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seller")
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<RegisterSellerDto.Response>> registerSeller(@RequestBody RegisterSellerDto.Request sellerInfo) {
        RegisterSellerDto.Response res = sellerService.registerSeller(sellerInfo);
        return ResponseEntity.ok(ApiResponse.created(res));
    }

    @PutMapping("/info")
    public ResponseEntity<ApiResponse<Object>> updateSellerInfo(@AuthenticationPrincipal UserDetails user, @RequestBody UpdateSellerDto.Request sellerInfo) {
        sellerService.updateSellerInfo(user, sellerInfo);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
