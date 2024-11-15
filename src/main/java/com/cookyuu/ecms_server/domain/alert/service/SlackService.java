package com.cookyuu.ecms_server.domain.alert.service;

public interface SlackService {
    String sendErrorForSlack(Exception exception);
}
