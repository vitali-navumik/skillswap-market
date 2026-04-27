package com.vitali.framework.api.offer;

import com.vitali.framework.api.offer.requests.CreateOfferRequest;
import com.vitali.framework.api.offer.requests.UpdateOfferRequest;
import com.vitali.framework.api.offer.responses.CreateOfferResponse;
import com.vitali.framework.api.offer.responses.GetOfferResponse;
import com.vitali.framework.api.offer.responses.UpdateOfferResponse;
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
    public ConnectorResponse<CreateOfferResponse> createOffer(CreateOfferRequest request) {
        return sender.send(OfferApi.createOffer(request));
    }

    @Step("Create offer with idempotency key")
    public ConnectorResponse<CreateOfferResponse> createOffer(CreateOfferRequest request, String idempotencyKey) {
        return sender.send(OfferApi.createOffer(request, idempotencyKey));
    }

    @Step("Create offer")
    public CreateOfferResponse createOfferResponse(CreateOfferRequest request) {
        return createOffer(request)
                .ifOk()
                .getDataResponse(new TypeRef<>() {
                });
    }

    @Step("Create offer with idempotency key")
    public CreateOfferResponse createOfferResponse(CreateOfferRequest request, String idempotencyKey) {
        return createOffer(request, idempotencyKey)
                .ifOk()
                .getDataResponse(new TypeRef<>() {
                });
    }

    @Step("Get offer")
    public ConnectorResponse<GetOfferResponse> getOffer(UUID publicId) {
        return sender.send(OfferApi.getOffer(publicId));
    }

    @Step("Get offer")
    public GetOfferResponse getOfferResponse(UUID publicId) {
        return getOffer(publicId)
                .ifOk()
                .getDataResponse(new TypeRef<>() {
                });
    }

    @Step("Update offer")
    public ConnectorResponse<UpdateOfferResponse> updateOffer(UpdateOfferRequest request) {
        return sender.send(OfferApi.updateOffer(request));
    }

    @Step("Update offer")
    public UpdateOfferResponse updateOfferResponse(UpdateOfferRequest request) {
        return updateOffer(request)
                .ifOk()
                .getDataResponse(new TypeRef<>() {
                });
    }
}
