package com.vitali.framework.connectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

public class RestAssuredResponse<R> implements ConnectorResponse<R> {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .findAndAddModules()
            .build();

    Response response;

    public RestAssuredResponse(Response response) {
        this.response = response;
    }

    @Override
    public <T> T getDataResponse(TypeRef<T> typeRef) {
        return response.as(typeRef);
    }

    @Override
    public String getDataResponse() {
        return response.getBody().asString();
    }

    @Override
    public <T> T as(Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(response.getBody().asString(), type);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to deserialize response to " + type.getSimpleName(), ex);
        }
    }

    @Override
    public String getHeader(String name) {
        return response.getHeader(name);
    }

    @Override
    public String getCookie(String name) {
        return response.getCookie(name);
    }

    @Override
    public int getResponseCode() {
        return response.statusCode();
    }
}
