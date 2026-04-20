package com.vitali.framework.resolvers;

import com.vitali.framework.api.login.response.AuthResponse;
import com.vitali.framework.api.login.LoginApi;
import com.vitali.framework.api.users.UsersActions;
import com.vitali.framework.api.users.UsersApi;
import com.vitali.framework.api.users.responses.CreateUserResponse;
import com.vitali.framework.api.users.responses.GetUserResponse;
import com.vitali.framework.connectors.RestAssuredConnector;
import com.vitali.framework.connectors.Sender;
import com.vitali.framework.api.users.requests.CreateUserRequest;
import com.vitali.framework.enums.UserPreset;
import io.qameta.allure.Step;
import io.restassured.common.mapper.TypeRef;
import lombok.experimental.UtilityClass;

@UtilityClass
public  class UserCreationHelper {

    @Step("Create and login user with preset: {0}")
    public static ActionsContainer createUserAndLogIn(UserPreset preset) {
        RestAssuredConnector connector = new RestAssuredConnector();

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .roles(preset.getRoles())
                .build();

        CreateUserResponse createdUser = createUser(connector, userRequest);
        String token = loginUser(connector, createdUser, userRequest);

        Sender sender = new Sender(token, connector);
        GetUserResponse userInfo = new UsersActions(sender).getUserResponse(createdUser.getPublicId());
        return ActionsFactory.createGlobalActions(sender, userInfo);
    }

    @Step("Create user")
    private static CreateUserResponse createUser(RestAssuredConnector connector, CreateUserRequest userRequest) {
        return connector.send(UsersApi.createUser(userRequest)
                        .authorized(AdminTokenProvider.getAdminToken(connector))
                        .build())
                .ifOk()
                .getDataResponse(new TypeRef<>() {
                });
    }

    @Step("Login user and get token")
    private static String loginUser(RestAssuredConnector connector, CreateUserResponse userInfo, CreateUserRequest userRequest) {
        AuthResponse response = connector.send(LoginApi.login(userInfo.getEmail(), userRequest.getPassword())
                        .build())
                .ifOk()
                .getDataResponse(new TypeRef<>() {
                });
        return response.getAccessToken();
    }
}
