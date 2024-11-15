package com.cookyuu.ecms_server.domain.alert.service;

import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.domain.ECMSAlertException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.Slack;
import com.slack.api.webhook.WebhookResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackAlertService implements SlackService{
    private final HttpServletRequest httpRequest;

    @Value(value = "${slack.webhook.url}")
    private String slackAlertWebhookUrl;

    @Override
    public String sendErrorForSlack(Exception exception) {
        Slack slack = Slack.getInstance();
        WebhookResponse response = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> slackMessage = new HashMap<>();
            StringWriter writer = new StringWriter();
            exception.printStackTrace(new PrintWriter(writer));

            String errorTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String errorPath = httpRequest.getRequestURI().toString();
            String exceptionName = exception.getClass().toString();
            String exceptionRoot = exception.getStackTrace()[0].toString();
            String exceptionMessage = exception.getMessage();
            String message = String.format("[%s] - [ECMS-Monitoring] - [%s] - [%s] - [%s] - [%s]", errorTime, errorPath, exceptionName, exceptionRoot, exceptionMessage);

            slackMessage.put("text", message);
            response = slack.send(slackAlertWebhookUrl, objectMapper.writeValueAsString(slackMessage));
            return "[Alert::Slack] ECMS slack alert message sent : " + response.getCode();
        } catch (IOException e) {
            throw new ECMSAlertException(ResultCode.FAIL_ALERT_SLACK, e);
        }
    }
}
