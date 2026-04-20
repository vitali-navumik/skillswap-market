package com.vitali.framework.api.users;

import com.vitali.framework.api.users.requests.CreateUserRequest;
import com.vitali.framework.api.users.requests.UpdateUserRequest;
import com.vitali.framework.config.Config;
import com.vitali.framework.connectors.BaseAPIRequest;
import io.restassured.http.Method;

import java.util.Map;
import java.util.UUID;

public final class UsersApi {

    private static final String BASE_PATH = "/users";

    public static BaseAPIRequest.BaseAPIRequestBuilder getUsers() {
        return BaseAPIRequest.builder()
                .baseUri(Config.API_URL)
                .basePath(BASE_PATH)
                .path("")
                .method(Method.GET);
    }

    public static BaseAPIRequest.BaseAPIRequestBuilder createUser(CreateUserRequest user) {
        return BaseAPIRequest.builder()
                .baseUri(Config.API_URL)
                .basePath(BASE_PATH)
                .path("")
                .requestBody(user)
                .method(Method.POST);
    }

    public static BaseAPIRequest.BaseAPIRequestBuilder getUser(UUID publicId) {
        return BaseAPIRequest.builder()
                .baseUri(Config.API_URL)
                .basePath(BASE_PATH)
                .path("/{publicId}")
                .pathParams(Map.of("publicId", publicId))
                .method(Method.GET);
    }

    public static BaseAPIRequest.BaseAPIRequestBuilder updateUser(UpdateUserRequest user) {
        return BaseAPIRequest.builder()
                .baseUri(Config.API_URL)
                .basePath(BASE_PATH)
                .path("/{publicId}")
                .pathParams(Map.of("publicId", user.getPublicId()))
                .requestBody(user)
                .method(Method.PATCH);
    }
}
