package support;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class MockServer implements AutoCloseable {

    private static final String CREATE_OFFER_PATH = "/api/offers/save";

    private final WireMockServer server = new WireMockServer(0);

    public void start() {
        server.start();
    }

    @Override
    public void close() {
        server.stop();
    }

    public String baseUrl() {
        return server.baseUrl();
    }

    public void stubCreateOfferReturns500() {
        server.stubFor(post(urlEqualTo(CREATE_OFFER_PATH))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Internal server error\"}")));
    }

    public void verifyCreateOfferCalledTimes(int times) {
        server.verify(times, postRequestedFor(urlEqualTo(CREATE_OFFER_PATH)));
    }
}
