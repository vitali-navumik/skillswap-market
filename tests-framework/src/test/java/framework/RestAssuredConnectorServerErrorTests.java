package framework;

import com.vitali.framework.api.offer.requests.CreateOfferRequest;
import com.vitali.framework.api.offer.responses.CreateOfferResponse;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.connectors.RestAssuredConnector;
import com.vitali.framework.connectors.Sender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.MockServer;
import support.actions.MockOfferActions;

import static org.assertj.core.api.Assertions.assertThat;

class RestAssuredConnectorServerErrorTests {

    @Test
    @DisplayName("Connector retries and returns server error")
    void connectorRetriesAndReturnsServerError() {
        try (MockServer mockServer = new MockServer()) {
            mockServer.start();
            mockServer.stubCreateOfferReturns500();

            MockOfferActions offerActions = new MockOfferActions(
                    new Sender("test-token", new RestAssuredConnector()),
                    mockServer.baseUrl()
            );
            CreateOfferRequest request = CreateOfferRequest.builder().build();

            ConnectorResponse<CreateOfferResponse> response = offerActions.createOffer(request);

            assertThat(response.statusCode()).isEqualTo(500);
            assertThat(response.getDataResponse()).contains("Internal server error");
            mockServer.verifyCreateOfferCalledTimes(3);
        }
    }
}
