package com.cookyuu.ecms_server.global.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class DateTimeUtils {
    public static LocalDateTime convertFromString(String pattern, String date) {
        DateTimeFormatter dateFormatter  = DateTimeFormatter.ofPattern(pattern);

        DateTimeFormatter LocalDateTimeFormatter = new DateTimeFormatterBuilder().append(dateFormatter)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .toFormatter();

        LocalDateTime ldt = LocalDateTime.parse(date, LocalDateTimeFormatter);
        return ldt;
    }
}
