package com.vitali.framework.api.login;

import com.vitali.framework.config.Config;
import com.vitali.framework.connectors.BaseAPIRequest;
import io.restassured.http.Method;

import java.util.Map;

public class LoginApi {
    private static final String BASE_PATH = "/auth";

    public static BaseAPIRequest.BaseAPIRequestBuilder login(String email, String password) {
        return BaseAPIRequest.builder()
                .baseUri(Config.API_URL)
                .basePath(BASE_PATH)
                .path("/login")
                .requestBody(Map.of("email", email, "password", password))
                .method(Method.POST);
    }
}
