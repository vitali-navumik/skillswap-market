package com.vitali.framework.api.auth;

import com.vitali.framework.api.auth.requests.RegisterRequest;
import com.vitali.framework.api.auth.responses.RegisterResponse;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.connectors.Sender;
import io.qameta.allure.Step;

public final class AuthActions {

    private final Sender sender;

    public AuthActions(Sender sender) {
        this.sender = sender;
    }

    @Step("Register user")
    public ConnectorResponse<RegisterResponse> register(RegisterRequest request) {
        return sender.send(AuthApi.register(request));
    }

    @Step("Register user")
    public RegisterResponse registerResponse(RegisterRequest request) {
        return register(request)
                .ifOk()
                .as(RegisterResponse.class);
    }
}
