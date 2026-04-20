package register;

import com.vitali.framework.CommonAssertions;
import com.vitali.framework.api.register.RegisterActions;
import com.vitali.framework.api.register.assertions.RegisterAssertions;
import com.vitali.framework.api.register.invocations.RegisterUserRequiredFieldsInvocation;
import com.vitali.framework.api.register.invocations.RegisterUserRequiredFieldsInvocation.RegisterRequiredFieldTestCase;
import com.vitali.framework.api.register.requests.RegisterUserRequest;
import com.vitali.framework.api.register.responses.RegisterUserResponse;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.connectors.RestAssuredConnector;
import com.vitali.framework.connectors.Sender;
import com.vitali.framework.enums.UserRole;
import com.vitali.framework.enums.UserStatus;
import com.vitali.framework.tags.RegisterTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RegisterTag
class RegisterTests {

    private final RegisterActions registerActions = new RegisterActions(new Sender(null, new RestAssuredConnector()));

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"STUDENT", "MENTOR"})
    void guestCanRegisterUser(UserRole role) {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .roles(Set.of(role))
                .build();

        RegisterUserResponse registerUserResponse = registerActions.registerResponse(request);
        RegisterAssertions.checkRegistrationDataIsCorrect(registerUserResponse, new RegisterAssertions.AssertionParams()
                .idExpected(true)
                .publicIdExpected(true)
                .email(request.getEmail())
                .roles(request.getRoles())
                .status(UserStatus.ACTIVE.name()));
    }

    @Test
    void guestCannotRegisterAdminUser() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .roles(Set.of(UserRole.ADMIN))
                .build();

        ConnectorResponse<RegisterUserResponse> response = registerActions.register(request);

        CommonAssertions.checkForbidden(response);
        assertThat(response.getDataResponse())
                .as("ADMIN role is not allowed in public registration")
                .contains("ADMIN role cannot be assigned through public registration");
    }

    @Test
    void guestCannotRegisterWithDuplicateEmail() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .roles(Set.of(UserRole.STUDENT))
                .build();

        CommonAssertions.checkResponseIsOk(registerActions.register(request));

        ConnectorResponse<RegisterUserResponse> duplicateResponse = registerActions.register(request);

        CommonAssertions.checkConflict(duplicateResponse);
        assertThat(duplicateResponse.getDataResponse())
                .as("Duplicated email is not allowed")
                .contains("Email is already in use");
    }

    @Test
    void guestCannotRegisterWithWeakPassword() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .roles(Set.of(UserRole.STUDENT))
                .password("weakPass")
                .build();

        ConnectorResponse<RegisterUserResponse> response = registerActions.register(request);

        CommonAssertions.checkUnprocessableEntityWithErrorMessage(response, new CommonAssertions.Params()
                .errorMessage("Password must contain at least one uppercase letter, one lowercase letter, and one digit"));
    }

    @TestTemplate
    @ExtendWith(RegisterUserRequiredFieldsInvocation.class)
    void guestCannotRegisterWithoutRequiredFields(RegisterRequiredFieldTestCase testCase) {
        RegisterUserRequest request = testCase.buildRequest();

        ConnectorResponse<RegisterUserResponse> response = registerActions.register(request);
        testCase.assertResult(response);
    }
}
