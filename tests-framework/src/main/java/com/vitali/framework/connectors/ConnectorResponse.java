package com.vitali.framework.connectors;

import io.qameta.allure.Step;
import io.restassured.common.mapper.TypeRef;

public interface ConnectorResponse<R> {
    <T> T getDataResponse(TypeRef<T> typeRef);

    String getDataResponse();

    <T> T as(Class<T> type);

    String getHeader(String name);

    String getCookie(String name);

    default boolean isOk() {
        return getResponseCode() < 400;
    }

    default boolean isNotOk() {
        return getResponseCode() >= 400;
    }

    int getResponseCode();

    default int statusCode() {
        return getResponseCode();
    }

    @Step("Check response is ok")
    default ConnectorResponse<R> ifOk() {
        if (isOk()) {
            return this;
        } else {
            throw new AssertionError(String.format("Response was not ok = %s", getResponseCode()));
        }
    }
}
