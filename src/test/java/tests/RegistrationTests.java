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
@Feature("Регистрация")
public class RegistrationTests extends BaseTest {

    private String testEmail;
    private static final String VALID_PASSWORD = "Password123";
    private static final String TEST_USER_NAME = "TestUser";

    @Before
    public void setUp() {
        testEmail = generateUniqueEmail();
    }

    // === Успешная регистрация: отдельные тесты для каждой проверки ===

    @Test
    @DisplayName("Статус ответа при успешной регистрации")
    @Description("Проверка, что успешная регистрация возвращает статус 200")
    public void checkStatusCodeOnSuccessfulRegistration() {
        UserCredentials credentials = new UserCredentials(testEmail, VALID_PASSWORD, TEST_USER_NAME);
        registerUser(credentials).statusCode(STATUS_OK);
    }

    @Test
    @DisplayName("Поле success при успешной регистрации")
    @Description("Проверка, что success=true при успешной регистрации")
    public void checkSuccessFieldOnSuccessfulRegistration() {
        UserCredentials credentials = new UserCredentials(testEmail, VALID_PASSWORD, TEST_USER_NAME);
        registerUser(credentials).body("success", equalTo(true));
    }

    @Test
    @DisplayName("Email пользователя в ответе при успешной регистрации")
    @Description("Проверка корректности email в ответе")
    public void checkUserEmailOnSuccessfulRegistration() {
        UserCredentials credentials = new UserCredentials(testEmail, VALID_PASSWORD, TEST_USER_NAME);
        registerUser(credentials).body("user.email", equalTo(testEmail));
    }

    @Test
    @DisplayName("Имя пользователя в ответе при успешной регистрации")
    @Description("Проверка корректности name в ответе")
    public void checkUserNameOnSuccessfulRegistration() {
        UserCredentials credentials = new UserCredentials(testEmail, VALID_PASSWORD, TEST_USER_NAME);
        registerUser(credentials).body("user.name", equalTo(TEST_USER_NAME));
    }

    // === Регистрация существующего пользователя: отдельные тесты ===

    @Test
    @DisplayName("Статус ответа при повторной регистрации")
    @Description("Проверка, что повторная регистрация возвращает 403 Forbidden")
    public void checkStatusCodeOnExistingUserRegistration() {
        // Сначала регистрируем пользователя
        String email = "existinguser" + System.currentTimeMillis() + "@yandex.ru";
        UserCredentials firstRegistration = new UserCredentials(email, VALID_PASSWORD, "ExistingUser");
        apiClient.registerUser(firstRegistration).statusCode(STATUS_OK);

        // Попытка зарегистрировать снова
        UserCredentials secondRegistration = new UserCredentials(email, VALID_PASSWORD, "ExistingUser");
        registerUser(secondRegistration).statusCode(STATUS_FORBIDDEN);
    }

    @Test
    @DisplayName("Поле success при повторной регистрации")
    @Description("Проверка, что success=false при повторной регистрации")
    public void checkSuccessFieldOnExistingUserRegistration() {
        String email = "existinguser" + System.currentTimeMillis() + "@yandex.ru";
        UserCredentials firstRegistration = new UserCredentials(email, VALID_PASSWORD, "ExistingUser");
        apiClient.registerUser(firstRegistration).statusCode(STATUS_OK);

        UserCredentials secondRegistration = new UserCredentials(email, VALID_PASSWORD, "ExistingUser");
        registerUser(secondRegistration).body("success", equalTo(false));
    }

    @Test
    @DisplayName("Сообщение об ошибке при повторной регистрации")
    @Description("Проверка текста ошибки при попытке зарегистрировать существующего пользователя")
    public void checkErrorMessageOnExistingUserRegistration() {
        String email = "existinguser" + System.currentTimeMillis() + "@yandex.ru";
        UserCredentials firstRegistration = new UserCredentials(email, VALID_PASSWORD, "ExistingUser");
        apiClient.registerUser(firstRegistration).statusCode(STATUS_OK);

        UserCredentials secondRegistration = new UserCredentials(email, VALID_PASSWORD, "ExistingUser");
        registerUser(secondRegistration)
                .body("message", containsString("User already exists"));
    }

    // === Ошибки при отсутствии обязательных полей: отдельные тесты ===

    @Test
    @DisplayName("Статус при регистрации без имени")
    @Description("Проверка статуса 400 при отсутствии поля name")
    public void checkStatusCodeWithoutName() {
        UserCredentials credentials = new UserCredentials(testEmail, VALID_PASSWORD);
        registerUser(credentials).statusCode(STATUS_BAD_REQUEST);
    }

    @Test
    @DisplayName("Поле success при регистрации без имени")
    @Description("Проверка success=false при отсутствии поля name")
    public void checkSuccessFieldWithoutName() {
        UserCredentials credentials = new UserCredentials(testEmail, VALID_PASSWORD);
        registerUser(credentials).body("success", equalTo(false));
    }

    @Test
    @DisplayName("Сообщение при регистрации без имени")
    @Description("Проверка сообщения об ошибке для отсутствующего поля name")
    public void checkErrorMessageWithoutName() {
        UserCredentials credentials = new UserCredentials(testEmail, VALID_PASSWORD);
        registerUser(credentials)
                .body("message", containsString("name is required"));
    }

    @Test
    @DisplayName("Статус при регистрации без email")
    @Description("Проверка статуса 400 при отсутствии поля email")
    public void checkStatusCodeWithoutEmail() {
        UserCredentials credentials = new UserCredentials(null, VALID_PASSWORD, TEST_USER_NAME);
        registerUser(credentials).statusCode(STATUS_BAD_REQUEST);
    }

    @Test
    @DisplayName("Сообщение при регистрации без email")
    @Description("Проверка сообщения об ошибке для отсутствующего поля email")
    public void checkErrorMessageWithoutEmail() {
        UserCredentials credentials = new UserCredentials(null, VALID_PASSWORD, TEST_USER_NAME);
        registerUser(credentials)
                .body("message", containsString("email is required"));
    }

    @Test
    @DisplayName("Статус при регистрации без пароля")
    @Description("Проверка статуса 400 при отсутствии поля password")
    public void checkErrorMessageWithoutPassword() {
        UserCredentials credentials = new UserCredentials(testEmail, null, TEST_USER_NAME);
        registerUser(credentials)
                .body("message", containsString("password is required"));
    }

    @Step("Регистрация пользователя")
    private ValidatableResponse registerUser(UserCredentials credentials) {
        return apiClient.registerUser(credentials);
    }
}
