import io.qameta.allure.*;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Пользователь")
@Feature("Регистрация и авторизация")
public class UserTests extends BaseTest {

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Проверка успешной регистрации нового пользователя")
    @Severity(SeverityLevel.CRITICAL)
    public void createUniqueUser() {
        // ARRANGE
        String email = generateUniqueEmail();
        String password = "Password123";
        String name = "TestUser";

        // ACT & ASSERT
        stepCreateUser(email, password, name)
                .statusCode(STATUS_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(email))
                .body("user.name", equalTo(name)); // Добавлена проверка имени пользователя
    }

    @Test
    @DisplayName("Создать пользователя, который уже зарегистрирован")
    @Description("Проверка отклика при повторной регистрации существующего пользователя")
    @Severity(SeverityLevel.NORMAL)
    public void createExistingUser() {
        // ARRANGE
        String email = "existinguser" + System.currentTimeMillis() + "@yandex.ru";
        String password = "Password123";
        String name = "ExistingUser";

        // Создаём пользователя первый раз
        stepCreateUser(email, password, name)
                .statusCode(STATUS_OK);

        // ACT: попытка зарегистрировать снова
        given()
                .spec(requestSpec)
                .body(String.format("{\"email\":\"%s\", \"password\":\"%s\", \"name\":\"%s\"}",
                        email, password, name))
                .when()
                .post(ENDPOINT_REGISTER)
                .then()
                // ASSERT
                .statusCode(STATUS_FORBIDDEN)
                .body("message", containsString("User already exists"))
                .body("success", equalTo(false)); // Добавлена проверка success=false
    }

    @Test
    @DisplayName("Создать пользователя с пропущенным обязательным полем")
    @Description("Попытка регистрации без имени")
    @Severity(SeverityLevel.CRITICAL)
    public void createUserWithoutRequiredField() {
        // ARRANGE
        String email = generateUniqueEmail();
        String password = "Password123";

        // ACT
        given()
                .spec(requestSpec)
                .body(String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password))
                .when()
                .post(ENDPOINT_REGISTER)
                .then()
                // ASSERT
                .statusCode(STATUS_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", containsString("name is required"));
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    @Description("Проверка успешного входа")
    @Severity(SeverityLevel.CRITICAL)
    public void loginUser() {
        // ARRANGE
        String email = generateUniqueEmail();
        String password = "Password123";
        registerAndGetToken(email, password, "TestUser"); // Предварительно регистрируем

        // ACT
        ValidatableResponse response = login(email, password);

        // ASSERT
        response
                .statusCode(STATUS_OK)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("user", notNullValue()) // Проверка существования объекта user
                .body("user.email", equalTo(email))
                .body("user.name", notNullValue()); // Проверка наличия имени пользователя
    }

    @Test
    @DisplayName("Логин с неправильными данными")
    @Description("Проверка ошибок при неправильных логине или пароле")
    @Severity(SeverityLevel.NORMAL)
    public void loginWithWrongCredentials() {
        // ARRANGE
        String wrongEmail = "wrongemail@yandex.ru";
        String wrongPassword = "WrongPassword";

        // ACT
        given()
                .spec(requestSpec)
                .body(String.format("{\"email\":\"%s\", \"password\":\"%s\"}", wrongEmail, wrongPassword))
                .when()
                .post(ENDPOINT_LOGIN)
                .then()
                // ASSERT
                .statusCode(STATUS_UNAUTHORIZED)
                .body("success", equalTo(false)) // Добавлена проверка success=false
                .body("message", containsString("email or password are incorrect"));
    }

    /**
     * Шаг для регистрации пользователя
     */
    @Step("Создать пользователя с email: {email}, password: {password}, name: {name}")
    protected ValidatableResponse stepCreateUser(String email, String password, String name) {
        return given()
                .spec(requestSpec)
                .body(String.format("{\"email\":\"%s\", \"password\":\"%s\", \"name\":\"%s\"}",
                        email, password, name))
                .when()
                .post(ENDPOINT_REGISTER)
                .then();
    }

    /**
     * Шаг для логина
     */
    @Step("Логин пользователя с email: {email}")
    protected ValidatableResponse login(String email, String password) {
        return given()
                .spec(requestSpec)
                .body(String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password))
                .when()
                .post(ENDPOINT_LOGIN)
                .then();
    }
}
