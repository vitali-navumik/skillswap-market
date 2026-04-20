package auth;

import com.vitali.framework.api.auth.AuthActions;
import com.vitali.framework.api.auth.assertions.AuthAssertions;
import com.vitali.framework.api.auth.requests.RegisterRequest;
import com.vitali.framework.api.auth.responses.RegisterResponse;
import com.vitali.framework.connectors.RestAssuredConnector;
import com.vitali.framework.connectors.Sender;
import com.vitali.framework.enums.UserRole;
import com.vitali.framework.enums.UserStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

class AuthTests {

    private final AuthActions authActions = new AuthActions(new Sender(null, new RestAssuredConnector()));

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"STUDENT", "MENTOR"})
    void guestCanRegisterUser(UserRole role) {
        RegisterRequest request = RegisterRequest.builder()
                .roles(Set.of(role))
                .build();

        RegisterResponse registerResponse = authActions.registerResponse(request);
        AuthAssertions.checkRegistrationDataIsCorrect(registerResponse, new AuthAssertions.AssertionParams()
                .idExpected(true)
                .publicIdExpected(true)
                .email(request.getEmail())
                .roles(request.getRoles())
                .status(UserStatus.ACTIVE.name()));
    }
}
