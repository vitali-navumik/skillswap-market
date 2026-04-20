package com.vitali.framework.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@AllArgsConstructor
public enum DateTimeFormats {
    DD_MM_YYYY("dd.MM.yyyy"),
    MM_DD_YYYY("MM-dd-yyyy"),
    SIMPLE_DATE("yyyy-MM-dd"),
    TIMESTAMP_WITH_SECONDS("yyyy-MM-dd'T'HH:mm:ss"),
    TIMESTAMP_WITH_MILLISECONDS("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
    TIME_HMS("HH:mm:ss"),
    TIME_HHMM_SHORT("HHmm");

    final String pattern;

    public DateTimeFormatter getFormatter() {
        return DateTimeFormatter.ofPattern(this.pattern);
    }
}
