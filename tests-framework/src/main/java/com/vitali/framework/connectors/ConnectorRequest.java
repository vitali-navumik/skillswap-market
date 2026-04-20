package com.vitali.framework.connectors;

import com.vitali.framework.resolvers.MultiPartParam;
import io.restassured.http.Method;

import java.util.List;
import java.util.Map;

public interface ConnectorRequest {
    String getBaseUri();

    Method getMethod();

    String getPath();

    Object getRequestBody();

    Map<String, ?> getPathParams();

    Map<String, ?> getQueryParams();

    Map<String, ?> getFormParams();

    Map<String, String> getHeaders();

    List<MultiPartParam> getMultiPartParams();

    String getContentType();

    Map<String, ?> getCookies();

    String getBasePath();
}
