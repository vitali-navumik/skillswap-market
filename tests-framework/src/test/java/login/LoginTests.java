package login;

import com.vitali.framework.CommonAssertions;
import com.vitali.framework.api.login.LoginActions;
import com.vitali.framework.api.login.assertions.LoginAssertions;
import com.vitali.framework.api.login.invocations.LoginRequiredFieldsInvocation;
import com.vitali.framework.api.login.invocations.LoginRequiredFieldsInvocation.LoginRequiredFieldTestCase;
import com.vitali.framework.api.login.response.LoginResponse;
import com.vitali.framework.api.register.RegisterActions;
import com.vitali.framework.api.register.requests.RegisterUserRequest;
import com.vitali.framework.api.users.requests.CreateUserRequest;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.connectors.RestAssuredConnector;
import com.vitali.framework.connectors.Sender;
import com.vitali.framework.enums.UserPreset;
import com.vitali.framework.enums.UserRole;
import com.vitali.framework.enums.UserStatus;
import com.vitali.framework.resolvers.ActionsContainer;
import com.vitali.framework.resolvers.GlobalActionsParameterResolver;
import com.vitali.framework.resolvers.GlobalActionsPreset;
import com.vitali.framework.tags.LoginTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Set;

@LoginTag
@ExtendWith({GlobalActionsParameterResolver.class})
public class LoginTests {

    private final RegisterActions registerActions = new RegisterActions(new Sender(null, new RestAssuredConnector()));
    private final LoginActions loginActions = new LoginActions(new Sender(null, new RestAssuredConnector()));

    @Test
    void guestCanLoginAfterSuccessfulRegistration() {
        RegisterUserRequest userRequest = RegisterUserRequest.builder()
                .roles(Set.of(UserRole.STUDENT))
                .build();
        CommonAssertions.checkResponseIsOk(registerActions.register(userRequest));

        LoginResponse loginResponse = loginActions.loginResponse(userRequest.getEmail(), userRequest.getPassword());
        LoginAssertions.checkLoginIsCorrect(loginResponse, new LoginAssertions.AssertionParams()
                .accessTokenExpected(true)
                .tokenType("Bearer")
                .expiresInExpected(true)
                .userExpected(true)
                .email(userRequest.getEmail()));
    }

    @Test
    void guestCanLoginWithNormalizedEmail() {
        String normalizedEmail = "user." + System.nanoTime() + "@example.com";
        RegisterUserRequest userRequest = RegisterUserRequest.builder()
                .email(normalizedEmail)
                .roles(Set.of(UserRole.STUDENT))
                .build();
        CommonAssertions.checkResponseIsOk(registerActions.register(userRequest));

        LoginResponse loginResponse = loginActions.loginResponse(normalizedEmail.toUpperCase(), userRequest.getPassword());

        LoginAssertions.checkLoginIsCorrect(loginResponse, new LoginAssertions.AssertionParams()
                .accessTokenExpected(true)
                .tokenType("Bearer")
                .expiresInExpected(true)
                .userExpected(true)
                .email(normalizedEmail));
    }

    @Test
    void inactiveUserCannotLogin(@GlobalActionsPreset(UserPreset.ADMIN) ActionsContainer admin) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .roles(Set.of(UserRole.STUDENT))
                .status(UserStatus.INACTIVE)
                .build();

        CommonAssertions.checkResponseIsOk(admin.usersActions().createUser(userRequest));

        ConnectorResponse<LoginResponse> response = loginActions.login(userRequest.getEmail(), userRequest.getPassword());
        LoginAssertions.checkInactiveUserError(response);
    }

    @Test
    void guestCannotLoginWithWrongPassword() {
        RegisterUserRequest userRequest = RegisterUserRequest.builder()
                .roles(Set.of(UserRole.STUDENT))
                .build();
        CommonAssertions.checkResponseIsOk(registerActions.register(userRequest));

        ConnectorResponse<LoginResponse> response = loginActions.login(userRequest.getEmail(), userRequest.getPassword() + "Wrong1");

        LoginAssertions.checkWrongPasswordError(response);
    }

    @Test
    void guestCannotLoginWithUnknownEmail() {
        ConnectorResponse<LoginResponse> response = loginActions.login("unknown+" + System.nanoTime() + "@example.com", "StrongPass1");

        LoginAssertions.checkUnknownEmailError(response);
    }

    @TestTemplate
    @ExtendWith(LoginRequiredFieldsInvocation.class)
    void guestCannotLoginWithoutRequiredFields(LoginRequiredFieldTestCase testCase) {
        ConnectorResponse<LoginResponse> response = loginActions.login(testCase.email(), testCase.password());

        testCase.assertResult(response);
    }

    @Test
    void guestCannotLoginWithWhitespaceOnlyCredentials() {
        ConnectorResponse<LoginResponse> response = loginActions.login("   ", "   ");

        LoginAssertions.checkBlankCredentialsValidationError(response);
    }

    @Test
    void guestCannotLoginWithEmailHavingLeadingOrTrailingSpaces() {
        RegisterUserRequest userRequest = RegisterUserRequest.builder()
                .roles(Set.of(UserRole.STUDENT))
                .build();
        CommonAssertions.checkResponseIsOk(registerActions.register(userRequest));

        ConnectorResponse<LoginResponse> response = loginActions.login(" " + userRequest.getEmail() + " ", userRequest.getPassword());

        LoginAssertions.checkInvalidEmailFormatValidationError(response);
    }
}
