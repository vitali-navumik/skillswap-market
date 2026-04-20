package com.vitali.framework.resolvers;

import com.vitali.framework.api.login.LoginApi;
import com.vitali.framework.api.login.response.AuthResponse;
import com.vitali.framework.config.Config;
import com.vitali.framework.connectors.RestAssuredConnector;
import io.qameta.allure.Step;
import io.restassured.common.mapper.TypeRef;

public final class AdminTokenProvider {

    private static volatile String adminToken;

    private AdminTokenProvider() {
    }

    public static String getAdminToken(RestAssuredConnector connector) {
        String cachedToken = adminToken;
        if (cachedToken != null && !cachedToken.isBlank()) {
            return cachedToken;
        }

        synchronized (AdminTokenProvider.class) {
            if (adminToken == null || adminToken.isBlank()) {
                adminToken = loginAsAdmin(connector);
            }
            return adminToken;
        }
    }

    static void reset() {
        adminToken = null;
    }

    @Step("Login as admin and cache token")
    private static String loginAsAdmin(RestAssuredConnector connector) {
        AuthResponse response = connector.send(LoginApi.login(Config.ADMIN_EMAIL, Config.ADMIN_PASSWORD)
                        .build())
                .ifOk()
                .getDataResponse(new TypeRef<AuthResponse>() {
                });
        return response.getAccessToken();
    }
}
