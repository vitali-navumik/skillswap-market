package com.vitali.framework.api.offer;

import com.vitali.framework.api.offer.requests.CreateOfferRequest;
import com.vitali.framework.api.offer.responses.OfferResponse;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.connectors.Sender;
import io.qameta.allure.Step;
import io.restassured.common.mapper.TypeRef;

import java.util.UUID;

public final class OfferActions {

    private final Sender sender;

    public OfferActions(Sender sender) {
        this.sender = sender;
    }

    @Step("Create offer")
    public ConnectorResponse<OfferResponse> createOffer(CreateOfferRequest request) {
        return sender.send(OfferApi.createOffer(request));
    }

    @Step("Create offer")
    public OfferResponse createOfferResponse(CreateOfferRequest request) {
        return createOffer(request)
                .ifOk()
                .getDataResponse(new TypeRef<>() {
                });
    }

    @Step("Get offer")
    public ConnectorResponse<OfferResponse> getOffer(UUID publicId) {
        return sender.send(OfferApi.getOffer(publicId));
    }

    @Step("Get offer")
    public OfferResponse getOfferResponse(UUID publicId) {
        return getOffer(publicId)
                .ifOk()
                .getDataResponse(new TypeRef<>() {
                });
    }
}
