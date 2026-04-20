package com.vitali.framework.api.login;

import com.vitali.framework.api.login.response.LoginResponse;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.connectors.Sender;
import io.qameta.allure.Step;
import io.restassured.common.mapper.TypeRef;

public final class LoginActions {

    private final Sender sender;

    public LoginActions(Sender sender) {
        this.sender = sender;
    }

    @Step("Login user")
    public ConnectorResponse<LoginResponse> login(String email, String password) {
        return sender.send(LoginApi.login(email, password));
    }

    @Step("Login user")
    public LoginResponse loginResponse(String email, String password) {
        return login(email, password)
                .ifOk()
                .getDataResponse(new TypeRef<>() {
                });
    }
}
