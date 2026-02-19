package tests;

import base.BaseTest;
import io.qameta.allure.*;
import io.qameta.allure.junit4.DisplayName;
import models.OrderRequest;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;

@Epic("Заказы")
@Feature("Создание и получение заказов")
public class OrderTests extends BaseTest {

    private String token;
    private List<String> ingredients;

    @Before
    public void setUp() {
        String email = generateUniqueEmail();
        String password = "Password123";
        token = registerAndGetToken(email, password, "TestUser");
        ingredients = getRandomIngredients(2);
    }

    // === Успешное создание заказа: отдельные тесты для каждой проверки ===

    @Test
    @DisplayName("Статус 200 при создании заказа с авторизацией")
    @Description("Проверка, что авторизованный пользователь получает статус 200")
    public void checkStatusCodeOnCreateOrderWithAuth() {
        OrderRequest orderRequest = new OrderRequest(ingredients);
        apiClient.createOrder(orderRequest, token).statusCode(STATUS_OK);
    }

    @Test
    @DisplayName("Поле success=true при успешном создании заказа")
    @Description("Проверка значения success в ответе")
    public void checkSuccessFieldOnCreateOrderWithAuth() {
        OrderRequest orderRequest = new OrderRequest(ingredients);
        apiClient.createOrder(orderRequest, token).body("success", equalTo(true));
    }

    @Test
    @DisplayName("Наличие объекта order в ответе")
    @Description("Проверка, что ответ содержит объект order")
    public void checkOrderObjectInResponse() {
        OrderRequest orderRequest = new OrderRequest(ingredients);
        apiClient.createOrder(orderRequest, token).body("order", notNullValue());
    }

    @Test
    @DisplayName("Наличие номера заказа в ответе")
    @Description("Проверка наличия поля order.number")
    public void checkOrderNumberInResponse() {
        OrderRequest orderRequest = new OrderRequest(ingredients);
        apiClient.createOrder(orderRequest, token).body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Наличие поля name в ответе")
    @Description("Проверка наличия поля name")
    public void checkNameFieldInResponse() {
        OrderRequest orderRequest = new OrderRequest(ingredients);
        apiClient.createOrder(orderRequest, token).body("name", notNullValue());
    }

    // === Создание заказа без авторизации: объединённый тест (ИСПРАВЛЕНИЕ 1) ===

    @Test
    @DisplayName("Проверка полного ответа при создании заказа без авторизации")
    @Description("Тест проверяет полный ответ API для неавторизованного запроса: статус 401, success=false, сообщение об ошибке")
    // ЗАМЕНЕНО: три отдельных теста на один комплексный
    public void checkFullResponseOnCreateOrderWithoutAuth() {
        OrderRequest orderRequest = new OrderRequest(ingredients);

        apiClient.createOrder(orderRequest, null)
                .statusCode(STATUS_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", containsString("You should be authorised"));
    }

    // Удалены старые тесты:
    // - checkStatusCodeOnCreateOrderWithoutAuth()
    // - checkSuccessFieldOnCreateOrderWithoutAuth()
    // - checkErrorMessageOnCreateOrderWithoutAuth()

    // === Другие сценарии: разбиваем на отдельные проверки ===

    @Test
    @DisplayName("Статус 200 при создании заказа с одним ингредиентом")
    @Description("Проверка статуса для заказа с минимально допустимым количеством ингредиентов")
    public void checkStatusCodeWithOneIngredient() {
        List<String> oneIngredient = getRandomIngredients(1);
        OrderRequest orderRequest = new OrderRequest(oneIngredient);
        apiClient.createOrder(orderRequest, token).statusCode(STATUS_OK);
    }

    // === Объединённый тест для пустого массива ингредиентов (ИСПРАВЛЕНИЕ 2) ===

    @Test
    @DisplayName("Проверка полного ответа при создании заказа без ингредиентов")
    @Description("Тест проверяет статус 400, success=false и сообщение 'Ingredient ids must be provided'")
    // ЗАМЕНЕНО: два отдельных теста на один комплексный
    public void checkFullErrorResponseWithoutIngredients() {
        OrderRequest emptyOrder = new OrderRequest(List.of());

        apiClient.createOrder(emptyOrder, token)
                .statusCode(STATUS_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    // Удален старый тест checkErrorResponseWithoutIngredients()
    // Старый тест checkStatusCodeWithoutIngredients() также удалён, его логика включена в новый тест

    @Test
    @DisplayName("Статус 500 при создании заказа с некорректными ингредиентами")
    @Description("Проверка статуса для некорректных данных ингредиентов")
    public void checkStatusCodeWithInvalidIngredients() {
        List<String> invalidIngredients = List.of("invalidHash1", "invalidHash2");
        OrderRequest orderRequest = new OrderRequest(invalidIngredients);
        apiClient.createOrder(orderRequest, token).statusCode(STATUS_INTERNAL_ERROR);
    }
}

