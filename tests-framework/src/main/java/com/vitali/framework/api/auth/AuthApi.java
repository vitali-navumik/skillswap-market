package com.vitali.framework.api.auth;

import com.vitali.framework.api.auth.requests.RegisterRequest;
import com.vitali.framework.config.Config;
import com.vitali.framework.connectors.BaseAPIRequest;
import io.restassured.http.Method;

public final class AuthApi {

    private static final String BASE_PATH = "/auth";

    private AuthApi() {
    }

    public static BaseAPIRequest.BaseAPIRequestBuilder register(RegisterRequest request) {
        return BaseAPIRequest.builder()
                .baseUri(Config.API_URL)
                .basePath(BASE_PATH)
                .path("/register")
                .requestBody(request)
                .method(Method.POST);
    }
}
