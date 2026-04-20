package com.vitali.framework.api.users;

import com.vitali.framework.api.users.responses.CreateUserResponse;
import com.vitali.framework.api.users.responses.GetUserResponse;
import com.vitali.framework.api.users.responses.UpdateUserResponse;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.connectors.Sender;
import com.vitali.framework.api.users.requests.CreateUserRequest;
import com.vitali.framework.api.users.requests.UpdateUserRequest;
import io.qameta.allure.Step;
import io.restassured.common.mapper.TypeRef;

import java.util.List;
import java.util.UUID;

public final class UsersActions {

    private final Sender sender;

    public UsersActions(Sender sender) {
        this.sender = sender;
    }

    @Step("Get users")
    public ConnectorResponse<List<GetUserResponse>> getUsers() {
        return sender.send(UsersApi.getUsers());
    }

    @Step("Get users")
    public List<GetUserResponse> getUsersResponse() {
        return getUsers()
                .ifOk()
                .getDataResponse(new TypeRef<>() {
                });
    }

    @Step("Create user")
    public ConnectorResponse<CreateUserResponse> createUser(CreateUserRequest request) {
        return sender.send(UsersApi.createUser(request));
    }

    @Step("Create user")
    public CreateUserResponse createUserResponse(CreateUserRequest request) {
        return createUser(request)
                .ifOk()
                .as(CreateUserResponse.class);
    }

    @Step("Get user")
    public ConnectorResponse<GetUserResponse> getUser(UUID publicId) {
        return sender.send(UsersApi.getUser(publicId));
    }

    @Step("Get user")
    public GetUserResponse getUserResponse(UUID publicId) {
        return getUser(publicId)
                .ifOk()
                .as(GetUserResponse.class);
    }

    @Step("Update user")
    public ConnectorResponse<UpdateUserResponse> updateUser(UpdateUserRequest request) {
        return sender.send(UsersApi.updateUser(request));
    }

    @Step("Update user")
    public UpdateUserResponse updateUserResponse(UpdateUserRequest request) {
        return updateUser(request)
                .ifOk()
                .as(UpdateUserResponse.class);
    }
}
