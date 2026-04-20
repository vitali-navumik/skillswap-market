package com.vitali.framework.api.register;

import com.vitali.framework.api.register.requests.RegisterUserRequest;
import com.vitali.framework.config.Config;
import com.vitali.framework.connectors.BaseAPIRequest;
import io.restassured.http.Method;

public final class RegisterApi {

    private static final String BASE_PATH = "/auth";

    private RegisterApi() {
    }

    public static BaseAPIRequest.BaseAPIRequestBuilder register(RegisterUserRequest request) {
        return BaseAPIRequest.builder()
                .baseUri(Config.API_URL)
                .basePath(BASE_PATH)
                .path("/register")
                .requestBody(request)
                .method(Method.POST);
    }
}
