package com.cookyuu.ecms_server.domain.alert.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/alert")
public class AlertController {
    @GetMapping
    public void test() throws Exception {
        throw new Exception("Alert Test Exception.");
    }


}
