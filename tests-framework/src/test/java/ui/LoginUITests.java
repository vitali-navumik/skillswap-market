package ui;

import com.vitali.framework.CommonAssertions;
import com.vitali.framework.GuestActions;
import com.vitali.framework.api.register.requests.RegisterUserRequest;
import com.vitali.framework.enums.UserRole;
import com.vitali.framework.tags.UiTag;
import com.vitali.framework.ui.Application;
import com.vitali.framework.ui.ApplicationResolver;
import com.vitali.framework.ui.catalog.CatalogPage;
import com.vitali.framework.ui.login.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Set;

@UiTag
@ExtendWith(ApplicationResolver.class)
public class LoginUITests {
    private final GuestActions guestActions = GuestActions.create();

    @Test
    @DisplayName("Student can login from UI")
    void studentCanLoginFromUi(Application app) {
        RegisterUserRequest user = RegisterUserRequest.builder()
                .roles(Set.of(UserRole.STUDENT))
                .build();
        CommonAssertions.checkResponseIsOk(guestActions.registerActions().register(user));

        app.getPage(LoginPage.class)
                .loginAs(user.getEmail(), user.getPassword())
                .assertions()
                .logInButtonDisappeared();

        app.getPage(CatalogPage.class)
                .assertions()
                .currentUrlIsCatalogPage();
    }

    @Test
    @DisplayName("Logged in user name is shown in header")
    void loggedInUserNameIsShownInHeader(Application app) {
        RegisterUserRequest user = RegisterUserRequest.builder()
                .roles(Set.of(UserRole.STUDENT))
                .build();
        CommonAssertions.checkResponseIsOk(guestActions.registerActions().register(user));

        app.getPage(LoginPage.class)
                .loginAs(user.getEmail(), user.getPassword());

        app.getPage(CatalogPage.class)
                .header()
                .assertions()
                .userMenuContainsName(user.getFirstName(), user.getLastName());
    }

    @Test
    @DisplayName("Guest sees login error for wrong password")
    void guestSeesLoginErrorForWrongPassword(Application app) {
        RegisterUserRequest user = RegisterUserRequest.builder()
                .roles(Set.of(UserRole.STUDENT))
                .build();
        CommonAssertions.checkResponseIsOk(guestActions.registerActions().register(user));

        app.getPage(LoginPage.class)
                .open()
                .enterEmail(user.getEmail())
                .enterPassword("WrongPass1")
                .clickLoginButton()
                .assertions()
                .errorMessageIs("Invalid email or password")
                .loginFormIsVisible();
    }
}
