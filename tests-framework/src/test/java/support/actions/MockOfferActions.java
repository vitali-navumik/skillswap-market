package support.actions;

import com.vitali.framework.api.offer.requests.CreateOfferRequest;
import com.vitali.framework.api.offer.responses.CreateOfferResponse;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.connectors.Sender;
import io.qameta.allure.Step;
import support.api.MockOfferApi;

public class MockOfferActions {

    private final Sender sender;
    private final String baseUri;

    public MockOfferActions(Sender sender, String baseUri) {
        this.sender = sender;
        this.baseUri = baseUri;
    }

    @Step("Create offer against mock server")
    public ConnectorResponse<CreateOfferResponse> createOffer(CreateOfferRequest request) {
        return sender.send(MockOfferApi.createOffer(baseUri, request));
    }
}
