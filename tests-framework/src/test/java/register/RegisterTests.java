package register;

import com.vitali.framework.CommonAssertions;
import com.vitali.framework.GuestActions;
import com.vitali.framework.api.register.assertions.RegisterAssertions;
import com.vitali.framework.api.register.invocations.RegisterUserRequiredFieldsInvocation;
import com.vitali.framework.api.register.invocations.RegisterUserRequiredFieldsInvocation.RegisterRequiredFieldTestCase;
import com.vitali.framework.api.register.requests.RegisterUserRequest;
import com.vitali.framework.api.register.responses.RegisterUserResponse;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.enums.UserRole;
import com.vitali.framework.enums.UserStatus;
import com.vitali.framework.tags.RegisterTag;
import com.vitali.framework.utils.FakerGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RegisterTag
class RegisterTests {

    private final GuestActions guestActions = GuestActions.create();

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"STUDENT", "MENTOR"})
    @DisplayName("Guest can register user")
    void guestCanRegisterUser(UserRole role) {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .roles(Set.of(role))
                .build();

        RegisterUserResponse registerUserResponse = guestActions.registerActions().registerResponse(request);
        RegisterAssertions.checkRegistrationDataIsCorrect(registerUserResponse, new RegisterAssertions.AssertionParams()
                .idExpected(true)
                .publicIdExpected(true)
                .email(request.getEmail())
                .roles(request.getRoles())
                .status(UserStatus.ACTIVE.name()));
    }

    @Test
    @DisplayName("Guest cannot register admin user")
    void guestCannotRegisterAdminUser() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .roles(Set.of(UserRole.ADMIN))
                .build();

        ConnectorResponse<RegisterUserResponse> response = guestActions.registerActions().register(request);

        CommonAssertions.checkForbidden(response);
        assertThat(response.getDataResponse())
                .as("ADMIN role is not allowed in public registration")
                .contains("ADMIN role cannot be assigned through public registration");
    }

    @Test
    @DisplayName("Guest cannot register with duplicate email")
    void guestCannotRegisterWithDuplicateEmail() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .roles(Set.of(UserRole.STUDENT))
                .build();

        CommonAssertions.checkResponseIsOk(guestActions.registerActions().register(request));

        ConnectorResponse<RegisterUserResponse> duplicateResponse = guestActions.registerActions().register(request);

        CommonAssertions.checkConflict(duplicateResponse);
        assertThat(duplicateResponse.getDataResponse())
                .as("Duplicated email is not allowed")
                .contains("Email is already in use");
    }

    @Test
    @DisplayName("Guest cannot register with multiple roles")
    void guestCannotRegisterWithMultipleRoles() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .roles(Set.of(UserRole.STUDENT, UserRole.MENTOR))
                .build();

        ConnectorResponse<RegisterUserResponse> response = guestActions.registerActions().register(request);

        CommonAssertions.checkUnprocessableEntityWithErrorMessage(response, new CommonAssertions.Params()
                .errorMessage("Select exactly one role"));
    }

    @Test
    @DisplayName("Guest can register with normalized email")
    void guestCanRegisterWithNormalizedEmail() {
        String normalizedEmail = FakerGenerator.randomEmail();

        RegisterUserRequest request = RegisterUserRequest.builder()
                .email(normalizedEmail.toUpperCase())
                .roles(Set.of(UserRole.STUDENT))
                .build();

        RegisterUserResponse registerUserResponse = guestActions.registerActions().registerResponse(request);

        RegisterAssertions.checkRegistrationDataIsCorrect(registerUserResponse, new RegisterAssertions.AssertionParams()
                .idExpected(true)
                .publicIdExpected(true)
                .email(normalizedEmail)
                .roles(request.getRoles())
                .status(UserStatus.ACTIVE.name()));
    }

    @Test
    @DisplayName("Guest cannot register with weak password")
    void guestCannotRegisterWithWeakPassword() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .roles(Set.of(UserRole.STUDENT))
                .password("weakPass")
                .build();

        ConnectorResponse<RegisterUserResponse> response = guestActions.registerActions().register(request);

        CommonAssertions.checkUnprocessableEntityWithErrorMessage(response, new CommonAssertions.Params()
                .errorMessage("Password must contain at least one uppercase letter, one lowercase letter, and one digit"));
    }

    @TestTemplate
    @ExtendWith(RegisterUserRequiredFieldsInvocation.class)
    @DisplayName("Guest cannot register without required fields")
    void guestCannotRegisterWithoutRequiredFields(RegisterRequiredFieldTestCase testCase) {
        RegisterUserRequest request = testCase.buildRequest();

        ConnectorResponse<RegisterUserResponse> response = guestActions.registerActions().register(request);
        testCase.assertResult(response);
    }
}
