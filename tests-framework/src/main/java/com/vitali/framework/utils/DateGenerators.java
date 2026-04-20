package com.vitali.framework.utils;

import com.vitali.framework.enums.DateTimeFormats;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public class DateGenerators {
    public static String getCurrentDate(DateTimeFormats format) {
        return LocalDateTime.now().format(format.getFormatter());
    }

    public static String format(LocalDateTime dateTime, DateTimeFormats format) {
        return format.getFormatter().format(dateTime);
    }

    public static String format(LocalTime time, DateTimeFormats format) {
        return format.getFormatter().format(time);
    }

    public static String format(LocalDate date, DateTimeFormats format) {
        return format.getFormatter().format(date);
    }

    public static String format(ZonedDateTime zonedDateTime, DateTimeFormats format) {
        return format.getFormatter().format(zonedDateTime);
    }

    public static String formatStartOfDay(LocalDate date, DateTimeFormats format) {
        return format(date.atStartOfDay(), format);
    }
}
