package com.vitali.framework;

import com.vitali.framework.connectors.ConnectorResponse;
import lombok.Data;
import lombok.experimental.Accessors;

import static io.qameta.allure.Allure.step;

import static org.assertj.core.api.Assertions.assertThat;

public final class CommonAssertions {

    private static final String RESPONSE_ERROR_CODE = "Response error code";
    private static final String RESPONSE_ERROR_MESSAGE = "Response error message";

    public static <R> void checkResponseIsOk(ConnectorResponse<R> response) {
        step("Check response is OK", () -> {
            assertThat(response.isOk()).as("Response should be OK").isTrue();
        });
    }

    public static <R> void checkResponseIsNotOk(ConnectorResponse<R> response) {
        step("Check Response is not OK", () -> {
            assertThat(response.isNotOk()).as(RESPONSE_ERROR_CODE).isTrue();
        });
    }

    public static <R> void checkBadRequest(ConnectorResponse<R> response) {
        step("Check Response error is 400 Bad Request", () -> {
            assertThat(response.getResponseCode()).as(RESPONSE_ERROR_CODE).isEqualTo(400);
        });
    }

    public static <R> void checkUnauthorized(ConnectorResponse<R> response) {
        step("Check Response error is 401 Unauthorized", () -> {
            assertThat(response.getResponseCode()).as(RESPONSE_ERROR_CODE).isEqualTo(401);
        });
    }

    public static <R> void checkForbidden(ConnectorResponse<R> response) {
        step("Check Response error is 403 Forbidden", () -> {
            assertThat(response.getResponseCode()).as(RESPONSE_ERROR_CODE).isEqualTo(403);
        });
    }

    public static <R> void checkNotFound(ConnectorResponse<R> response) {
        step("Check Response error is 404 Not Found", () -> {
            assertThat(response.getResponseCode()).as(RESPONSE_ERROR_CODE).isEqualTo(404);
        });
    }

    public static <R> void checkNotFoundWithErrorMessage(ConnectorResponse<R> response, Params params) {
        checkNotFound(response);
        step("Check if error message is correct", () -> {
            String responseBody = response.getDataResponse();
            assertThat(responseBody).as(RESPONSE_ERROR_MESSAGE)
                    .contains(params.errorMessage);
        });
    }

    public static <R> void checkInternalServerError(ConnectorResponse<R> response) {
        step("Check Response error is 500 Internal Server Error", () -> {
            assertThat(response.getResponseCode()).as(RESPONSE_ERROR_CODE).isEqualTo(500);
        });
    }

    public static <R> void checkConflict(ConnectorResponse<R> response) {
        step("Check Response error is 409 Conflict", () -> {
            assertThat(response.getResponseCode()).as(RESPONSE_ERROR_CODE).isEqualTo(409);
        });
    }

    public static <R> void checkCreated(ConnectorResponse<R> response) {
        step("Check Response is 201 Created", () -> {
            assertThat(response.getResponseCode()).as("Response status").isEqualTo(201);
        });
    }

    public static <R> void checkUnprocessableEntity(ConnectorResponse<R> response) {
        step("Check Response error is 422 Unprocessable Entity", () -> {
            assertThat(response.getResponseCode()).as(RESPONSE_ERROR_CODE).isEqualTo(422);
        });
    }

    public static <R> void checkUnprocessableEntityWithErrorMessage(ConnectorResponse<R> response, Params params) {
        checkUnprocessableEntity(response);
        step("Check if error message is correct", () -> {
            assertThat(response.getDataResponse()).as(RESPONSE_ERROR_MESSAGE)
                    .contains(params.errorMessage());
        });
    }

    @Data
    @Accessors(fluent = true)
    public static class Params {
        private String errorMessage;
    }
}
