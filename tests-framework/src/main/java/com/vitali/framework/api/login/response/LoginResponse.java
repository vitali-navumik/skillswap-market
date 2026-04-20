package com.vitali.framework.api.login.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private LoginUserResponse user;
}
