package com.cookyuu.ecms_server.global.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class StringUtils {
    public static LocalDateTime parseToLocalDateTime(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        log.debug("[String->LocalDateTime] Convert Data format.");
        return LocalDateTime.parse(dateString, formatter);
    }
}
