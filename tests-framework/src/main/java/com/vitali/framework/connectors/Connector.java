package com.vitali.framework.connectors;

public interface Connector {
    <T> ConnectorResponse<T> send(ConnectorRequest request);
}
