package com.vitali.framework.api.offer;

import com.vitali.framework.api.offer.requests.CreateOfferRequest;
import com.vitali.framework.config.Config;
import com.vitali.framework.connectors.BaseAPIRequest;
import io.restassured.http.Method;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class OfferApi {

    private static final String BASE_PATH = "/offers";

    private OfferApi() {
    }

    public static BaseAPIRequest.BaseAPIRequestBuilder createOffer(CreateOfferRequest request) {
        return createOffer(request, null);
    }

    public static BaseAPIRequest.BaseAPIRequestBuilder createOffer(CreateOfferRequest request, String idempotencyKey) {
        Map<String, String> headers = new HashMap<>();
        if (idempotencyKey != null) {
            headers.put("Idempotency-Key", idempotencyKey);
        }

        return BaseAPIRequest.builder()
                .baseUri(Config.API_URL)
                .basePath(BASE_PATH)
                .path("")
                .headers(headers)
                .requestBody(request)
                .method(Method.POST);
    }

    public static BaseAPIRequest.BaseAPIRequestBuilder getOffer(UUID publicId) {
        return BaseAPIRequest.builder()
                .baseUri(Config.API_URL)
                .basePath(BASE_PATH)
                .path("/{publicId}")
                .pathParams(Map.of("publicId", publicId))
                .method(Method.GET);
    }
}
