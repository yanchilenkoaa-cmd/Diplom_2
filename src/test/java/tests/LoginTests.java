package tests;

import base.BaseTest;
import io.qameta.allure.*;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import models.UserCredentials;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

@Epic("Пользователь")
@Feature("Авторизация")
public class LoginTests extends BaseTest {

    private static final String VALID_PASSWORD = "Password123";
    private static final String INVALID_PASSWORD = "WrongPassword";
    private static final String EMPTY_STRING = "";

    private String registeredEmail;

    @Before
    public void setUp() {
        registeredEmail = generateUniqueEmail();
        registerAndGetToken(registeredEmail, VALID_PASSWORD, "TestUser");
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    @Description("Проверка успешного входа")
    @Severity(SeverityLevel.CRITICAL)
    public void loginUser() {
        UserCredentials credentials = new UserCredentials(registeredEmail, VALID_PASSWORD);
        ValidatableResponse response = loginUser(credentials);

        response.statusCode(STATUS_OK);
        response.body(
                "success", equalTo(true),
                "accessToken", notNullValue(),
                "user", notNullValue(),
                "user.email", equalTo(registeredEmail),
                "user.name", notNullValue()
        );
    }

    @Test
    @DisplayName("Логин с неправильным email")
    @Description("Проверка ошибки при неверном email")
    @Severity(SeverityLevel.NORMAL)
    public void loginWithWrongEmail() {
        String wrongEmail = "wrongemail@yandex.ru";
        UserCredentials credentials = new UserCredentials(wrongEmail, VALID_PASSWORD);

        loginUser(credentials)
                .statusCode(STATUS_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", containsString("email or password are incorrect"));
    }

    @Test
    @DisplayName("Логин с неправильным паролем")
    @Description("Проверка ошибки при неверном пароле")
    @Severity(SeverityLevel.NORMAL)
    public void loginWithWrongPassword() {
        UserCredentials credentials = new UserCredentials(registeredEmail, INVALID_PASSWORD);

        loginUser(credentials)
                .statusCode(STATUS_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", containsString("email or password are incorrect"));
    }

    @Test
    @DisplayName("Логин с пустым email")
    @Description("Проверка реакции API на пустой email")
    @Severity(SeverityLevel.CRITICAL)
    public void loginWithEmptyEmail() {
        UserCredentials credentials = new UserCredentials(EMPTY_STRING, VALID_PASSWORD);

        loginUser(credentials)
                .statusCode(STATUS_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", containsString("email must be present"));
    }

    @Test
    @DisplayName("Логин с пустым паролем")
    @Description("Проверка реакции API на пустой пароль")
    @Severity(SeverityLevel.CRITICAL)
    public void loginWithEmptyPassword() {
        UserCredentials credentials = new UserCredentials(registeredEmail, EMPTY_STRING);

        loginUser(credentials)
                .statusCode(STATUS_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", containsString("password must be present"));
    }

    @Step("Логин пользователя")
    private ValidatableResponse loginUser(UserCredentials credentials) {
        return apiClient.loginUser(credentials);
    }
}
