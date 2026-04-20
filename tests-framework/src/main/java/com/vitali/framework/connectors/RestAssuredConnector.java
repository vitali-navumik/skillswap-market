package com.vitali.framework.connectors;

import com.vitali.framework.resolvers.MultiPartParam;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;

import java.util.List;
import java.util.logging.Logger;

public final class RestAssuredConnector implements Connector {
    private static final Logger LOGGER = Logger.getLogger(RestAssuredConnector.class.getName());

    @SneakyThrows
    public <T> ConnectorResponse<T> send(ConnectorRequest request) {
        int maxRetries = 3;
        Response lastResponse = null;

        while (maxRetries-- > 0) {
            LOGGER.info(String.format("Sending %s request to %s", request.getMethod(), request.getPath()));

            lastResponse = RestAssured.given(getRequestSpecification(request))
                    .request(request.getMethod(), request.getPath());

            if (lastResponse.getStatusCode() < 500) {
                return new RestAssuredResponse<>(lastResponse);
            }

            Thread.sleep(1000);
            LOGGER.warning(String.format("Request failed with status %d. Retrying...", lastResponse.getStatusCode()));
        }

        return new RestAssuredResponse<>(lastResponse);
    }

    private RequestSpecification getRequestSpecification(ConnectorRequest request) {
        RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
        requestSpecBuilder.setRelaxedHTTPSValidation();
        requestSpecBuilder.addFilters(List.of(new AllureRestAssured(), new RequestLoggingFilter(), new ResponseLoggingFilter()));
        requestSpecBuilder.setBaseUri(request.getBaseUri());

        if (request.getBasePath() != null) requestSpecBuilder.setBasePath(request.getBasePath());
        if (request.getPathParams() != null) requestSpecBuilder.addPathParams(request.getPathParams());
        if (request.getQueryParams() != null) requestSpecBuilder.addQueryParams(request.getQueryParams());
        if (request.getFormParams() != null) requestSpecBuilder.addFormParams(request.getFormParams());
        if (request.getHeaders() != null) requestSpecBuilder.addHeaders(request.getHeaders());
        if (request.getContentType() != null) requestSpecBuilder.setContentType(request.getContentType());
        if (request.getCookies() != null) requestSpecBuilder.addCookies(request.getCookies());
        if (request.getRequestBody() != null) requestSpecBuilder.setBody(request.getRequestBody());


        if (!request.getMultiPartParams().isEmpty()) {
            RequestSpecification spec = requestSpecBuilder.build();
            for (MultiPartParam param : request.getMultiPartParams()) {
                if (param.getContentType() != null) {
                    spec.multiPart(param.getName(), param.getFilename(), param.getValue(), param.getContentType());
                } else {
                    spec.multiPart(param.getName(), param.getValue());
                }
            }
            return spec;
        }

        return requestSpecBuilder.build();
    }
}
