package com.vitali.framework.api.register;

import com.vitali.framework.api.register.requests.RegisterUserRequest;
import com.vitali.framework.api.register.responses.RegisterUserResponse;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.connectors.Sender;
import io.qameta.allure.Step;

public final class RegisterActions {

    private final Sender sender;

    public RegisterActions(Sender sender) {
        this.sender = sender;
    }

    @Step("Register user")
    public ConnectorResponse<RegisterUserResponse> register(RegisterUserRequest request) {
        return sender.send(RegisterApi.register(request));
    }

    @Step("Register user")
    public RegisterUserResponse registerResponse(RegisterUserRequest request) {
        return register(request)
                .ifOk()
                .as(RegisterUserResponse.class);
    }
}
