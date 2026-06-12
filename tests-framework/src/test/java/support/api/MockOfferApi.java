package support.api;

import com.vitali.framework.api.offer.requests.CreateOfferRequest;
import com.vitali.framework.connectors.BaseAPIRequest;
import io.restassured.http.Method;

public final class MockOfferApi {

    private static final String BASE_PATH = "/api/offers";

    private MockOfferApi() {
    }

    public static BaseAPIRequest.BaseAPIRequestBuilder createOffer(String baseUri, CreateOfferRequest request) {
        return BaseAPIRequest.builder()
                .baseUri(baseUri)
                .basePath(BASE_PATH)
                .path("/save")
                .requestBody(request)
                .method(Method.POST);
    }
}
