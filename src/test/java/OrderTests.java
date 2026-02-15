import io.qameta.allure.*;
import io.qameta.allure.junit4.DisplayName;
import org.junit.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;

@Epic("Заказы")
@Feature("Создание и получение заказов")
public class OrderTests extends BaseTest {

    @Test
    @DisplayName("Создать заказ с авторизацией")
    @Description("Авторизованный пользователь создает заказ с несколькими ингредиентами")
    @Severity(SeverityLevel.BLOCKER)
    public void testCreateOrderWithAuth() {
        // ARRANGE
        String email = generateUniqueEmail();
        String password = "Password123";
        String token = registerAndGetToken(email, password, "TestUser");
        assertNotNull("Токен не был получен после регистрации", token); // Проверка получения токена

        List<String> ingredients = getRandomIngredients(2);

        // ACT
        given()
                .spec(requestSpec)
                .header(HEADER_AUTHORIZATION, "Bearer " + token)
                .body(createOrderBody(ingredients))
                .when()
                .post(ENDPOINT_ORDERS)
                .then()
                // ASSERT
                .statusCode(STATUS_OK)
                .body("success", equalTo(true))
                .body("order", notNullValue()) // Проверка существования объекта order
                .body("order.number", notNullValue())
                .body("name", notNullValue());
    }

    @Test
    @DisplayName("Создать заказ без авторизации")
    @Description("Попытка создания заказа без авторизации должна вернуть 401")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreateOrderWithoutAuth() {
        List<String> ingredients = getRandomIngredients(2);

        given()
                .spec(requestSpec)
                .body(createOrderBody(ingredients))
                .when()
                .post(ENDPOINT_ORDERS)
                .then()
                .statusCode(STATUS_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", containsString("You should be authorised"));
    }

    @Test
    @DisplayName("Создать заказ с одним ингредиентом")
    @Description("Проверка создания заказа с минимально допустимым количеством ингредиентов")
    @Severity(SeverityLevel.NORMAL)
    public void testCreateOrderWithOneIngredient() {
        // ARRANGE
        String email = generateUniqueEmail();
        String password = "Password123";
        String token = registerAndGetToken(email, password, "TestUser");
        assertNotNull("Токен не был получен после регистрации", token);

        List<String> ingredients = getRandomIngredients(1);

        // ACT
        given()
                .spec(requestSpec)
                .header(HEADER_AUTHORIZATION, "Bearer " + token)
                .body(createOrderBody(ingredients))
                .when()
                .post(ENDPOINT_ORDERS)
                .then()
                // ASSERT
                .statusCode(STATUS_OK)
                .body("success", equalTo(true))
                .body("order", notNullValue())
                .body("order.number", notNullValue())
                .body("name", notNullValue());
    }

    @Test
    @DisplayName("Создать заказ без ингредиентов")
    @Description("Проверка реакции API при отправке пустого массива ингредиентов")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreateOrderWithoutIngredients() {
        String email = generateUniqueEmail();
        String password = "Password123";
        String token = registerAndGetToken(email, password, "TestUser");
        assertNotNull("Токен не был получен после регистрации", token);

        given()
                .spec(requestSpec)
                .header(HEADER_AUTHORIZATION, "Bearer " + token)
                .body("{\"ingredients\": []}")
                .when()
                .post(ENDPOINT_ORDERS)
                .then()
                .statusCode(STATUS_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создать заказ с некорректными ингредиентами")
    @Description("Отправка некорректных данных при создании заказа должна вернуть 500")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreateOrderWithInvalidIngredients() {
        String email = generateUniqueEmail();
        String password = "Password123";
        String token = registerAndGetToken(email, password, "TestUser");
        assertNotNull("Токен не был получен после регистрации", token);

        List<String> invalidIngredients = List.of("invalidHash1", "invalidHash2");

        given()
                .spec(requestSpec)
                .header(HEADER_AUTHORIZATION, "Bearer " + token)
                .body(createOrderBody(invalidIngredients))
                .when()
                .post(ENDPOINT_ORDERS)
                .then()
                .statusCode(STATUS_INTERNAL_ERROR);
    }
}
