package com.vitali.framework.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ContentType {
    APPLICATION_JSON("application/json"),
    MULTIPART_FORM_DATA("multipart/form-data");

    private final String value;
}
