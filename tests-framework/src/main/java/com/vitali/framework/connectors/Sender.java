package com.vitali.framework.connectors;

public final class Sender {

    private final Connector connector;
    private final String token;

    public Sender(String token, Connector connector) {
        this.connector = connector;
        this.token = token;
    }

    public <T> ConnectorResponse<T> send(BaseAPIRequest.BaseAPIRequestBuilder requestBuilder) {
        return connector.send(requestBuilder
                .authorized(this.token)
                .build());
    }
}
