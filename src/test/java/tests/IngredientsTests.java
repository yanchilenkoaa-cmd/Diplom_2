package tests;

import base.BaseTest;
import io.qameta.allure.*;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

@Epic("Ингредиенты")
@Feature("Получение списка ингредиентов")
public class IngredientsTests extends BaseTest {

    private String token;

    @Before
    public void setUp() {
        // Регистрируем пользователя и получаем токен для сценариев с авторизацией
        String email = generateUniqueEmail();
        String password = "Password123";
        token = registerAndGetToken(email, password, "TestUser");
    }

    // === Успешное получение ингредиентов: проверки для авторизованного и неавторизованного запросов ===

    @Test
    @DisplayName("Статус 200 при получении ингредиентов с авторизацией")
    @Description("Проверка, что авторизованный пользователь получает статус 200")
    public void checkStatusCodeWithAuth() {
        ValidatableResponse response = getIngredients(token);
        response.statusCode(STATUS_OK);
    }

    @Test
    @DisplayName("Статус 200 при получении ингредиентов без авторизации")
    @Description("Проверка, что неавторизованный запрос также возвращает статус 200")
    public void checkStatusCodeWithoutAuth() {
        ValidatableResponse response = getIngredients(null);
        response.statusCode(STATUS_OK);
    }

    @Test
    @DisplayName("Наличие поля success в ответе")
    @Description("Проверка, что ответ содержит поле success=true")
    public void checkSuccessField() {
        ValidatableResponse response = getIngredients(null);
        response.body("success", equalTo(true));
    }

    @Test
    @DisplayName("Наличие массива data в ответе")
    @Description("Проверка, что ответ содержит массив data с ингредиентами")
    public void checkDataArrayInResponse() {
        ValidatableResponse response = getIngredients(null);
        response.body("data", notNullValue());
    }

    @Test
    @DisplayName("Структура элемента массива data")
    @Description("Проверка полей в каждом элементе массива ингредиентов")
    public void checkIngredientStructure() {
        ValidatableResponse response = getIngredients(null);

        response.body(
                "data[0]._id", notNullValue(),
                "data[0].name", notNullValue(),
                "data[0].type", notNullValue(),
                "data[0].price", greaterThan(0),
                "data[0].image", notNullValue()
        );
    }

    @Test
    @DisplayName("Минимальное количество ингредиентов в ответе")
    @Description("Проверка, что возвращается хотя бы 1 ингредиент")
    public void checkMinimumIngredientsCount() {
        ValidatableResponse response = getIngredients(null);
        response.body("data.size()", greaterThanOrEqualTo(1));
    }

    @Test
    @DisplayName("Корректность типов данных в ответе")
    @Description("Проверка типов полей для каждого ингредиента")
    public void checkDataTypesInResponse() {
        ValidatableResponse response = getIngredients(null);

        response.body(
                "data[0]._id", instanceOf(String.class),
                "data[0].name", instanceOf(String.class),
                "data[0].type", instanceOf(String.class),
                "data[0].price", instanceOf(Integer.class),
                "data[0].image", instanceOf(String.class)
        );
    }

    // === Вспомогательные методы ===

    @Step("Получение списка ингредиентов")
    private ValidatableResponse getIngredients(String token) {
        return apiClient.getIngredients(token);
    }
}

