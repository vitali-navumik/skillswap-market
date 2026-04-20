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
import com.vitali.framework.utils.FakerGenerator;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Guest can login after successful registration")
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
    @DisplayName("Guest can login with normalized email")
    void guestCanLoginWithNormalizedEmail() {
        String normalizedEmail = FakerGenerator.randomEmail();
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
    @DisplayName("Inactive user cannot login")
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
    @DisplayName("Guest cannot login with wrong password")
    void guestCannotLoginWithWrongPassword() {
        RegisterUserRequest userRequest = RegisterUserRequest.builder()
                .roles(Set.of(UserRole.STUDENT))
                .build();
        CommonAssertions.checkResponseIsOk(registerActions.register(userRequest));

        ConnectorResponse<LoginResponse> response = loginActions.login(userRequest.getEmail(), userRequest.getPassword() + "Wrong1");

        LoginAssertions.checkWrongPasswordError(response);
    }

    @Test
    @DisplayName("Guest cannot login with unknown email")
    void guestCannotLoginWithUnknownEmail() {
        ConnectorResponse<LoginResponse> response = loginActions.login(FakerGenerator.randomEmail(), "StrongPass1");

        LoginAssertions.checkUnknownEmailError(response);
    }

    @TestTemplate
    @ExtendWith(LoginRequiredFieldsInvocation.class)
    @DisplayName("Guest cannot login without required fields")
    void guestCannotLoginWithoutRequiredFields(LoginRequiredFieldTestCase testCase) {
        ConnectorResponse<LoginResponse> response = loginActions.login(testCase.email(), testCase.password());

        testCase.assertResult(response);
    }

    @Test
    @DisplayName("Guest cannot login with whitespace only credentials")
    void guestCannotLoginWithWhitespaceOnlyCredentials() {
        ConnectorResponse<LoginResponse> response = loginActions.login("   ", "   ");

        LoginAssertions.checkBlankCredentialsValidationError(response);
    }

    @Test
    @DisplayName("Guest cannot login with email having leading or trailing spaces")
    void guestCannotLoginWithEmailHavingLeadingOrTrailingSpaces() {
        RegisterUserRequest userRequest = RegisterUserRequest.builder()
                .roles(Set.of(UserRole.STUDENT))
                .build();
        CommonAssertions.checkResponseIsOk(registerActions.register(userRequest));

        ConnectorResponse<LoginResponse> response = loginActions.login(" " + userRequest.getEmail() + " ", userRequest.getPassword());

        LoginAssertions.checkInvalidEmailFormatValidationError(response);
    }
}
