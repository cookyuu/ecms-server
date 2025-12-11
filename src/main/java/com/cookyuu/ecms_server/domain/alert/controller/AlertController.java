package com.cookyuu.ecms_server.domain.alert.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "알림 API", description = "Slack 알림 테스트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/alert")
public class AlertController {

    @Operation(
        summary = "알림 테스트",
        description = "Slack 알림 기능을 테스트합니다. 의도적으로 예외를 발생시켜 Slack으로 에러 알림이 전송되는지 확인합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "테스트용 예외 발생")
    })
    @GetMapping
    public void test() throws Exception {
        throw new Exception("Alert Test Exception.");
    }
}
