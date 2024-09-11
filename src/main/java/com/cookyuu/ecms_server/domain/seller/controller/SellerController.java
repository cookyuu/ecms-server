package com.cookyuu.ecms_server.domain.seller.controller;

import com.cookyuu.ecms_server.domain.seller.dto.RegisterSellerDto;
import com.cookyuu.ecms_server.domain.seller.service.SellerService;
import com.cookyuu.ecms_server.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seller")
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/registration")
    public ResponseEntity<Object> registerSeller(@RequestBody RegisterSellerDto.Request sellerInfo) {
        sellerService.registerSeller(sellerInfo);
        return ResponseEntity.ok(ApiResponse.created());
    }
}
