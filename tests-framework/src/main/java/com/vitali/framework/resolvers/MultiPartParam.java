package com.vitali.framework.resolvers;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MultiPartParam {
    private final String name;
    private final String filename;
    private final Object value;
    private final String contentType;
}
