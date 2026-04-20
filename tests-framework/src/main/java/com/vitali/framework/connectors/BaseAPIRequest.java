package com.vitali.framework.connectors;

import com.vitali.framework.resolvers.MultiPartParam;
import io.restassured.http.Method;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class BaseAPIRequest implements ConnectorRequest {

    private final String baseUri;
    private final String path;
    private final Method method;
    private final Map<String, ?> pathParams;
    private final Map<String, ?> queryParams;
    private final Map<String, ?> formParams;
    private final List<MultiPartParam> multiPartParams;
    private final Map<String, String> headers;
    private final String contentType;
    private final Map<String, ?> cookies;
    private final Object requestBody;
    private final String basePath;

    public static class BaseAPIRequestBuilder {
        private String baseUri;
        private String path;
        private Method method;
        private Map<String, Object> pathParams = new LinkedHashMap<>();
        private Map<String, Object> queryParams = new LinkedHashMap<>();
        private Map<String, ?> formParams;
        private List<MultiPartParam> multiPartParams = new ArrayList<>();
        private Map<String, String> headers = new HashMap<>();
        private String contentType = "application/json";
        private Map<String, ?> cookies;
        private Object requestBody;
        private String basePath;
        private Supplier<String> tokenSupplier;

        public BaseAPIRequestBuilder authorized(Supplier<String> tokenSupplier) {
            this.tokenSupplier = tokenSupplier;
            return this;
        }

        public BaseAPIRequestBuilder authorized(String token) {
            this.tokenSupplier = () -> token;
            return this;
        }

        public BaseAPIRequestBuilder pathParam(String key, Object value) {
            this.pathParams.put(key, value);
            return this;
        }

        public BaseAPIRequestBuilder queryParam(String key, Object value) {
            this.queryParams.put(key, value);
            return this;
        }

        public BaseAPIRequestBuilder multiPartParam(String name, String filename, Object value, String contentType) {
            this.multiPartParams.add(new MultiPartParam(name, filename, value, contentType));
            return this;
        }

        public BaseAPIRequest build() {
            if (headers == null) {
                headers = new HashMap<>();
            } else {
                headers = new HashMap<>(headers);
            }

            if (tokenSupplier != null) {
                String token = tokenSupplier.get();
                if (token != null && !token.isBlank()) {
                    headers.put("Authorization", "Bearer " + token);
                }
            }

            if (!multiPartParams.isEmpty()) {
                contentType = "multipart/form-data";
            }
            return new BaseAPIRequest(baseUri, path, method, pathParams, queryParams, formParams, multiPartParams, headers, contentType, cookies, requestBody, basePath);
        }
    }
}
