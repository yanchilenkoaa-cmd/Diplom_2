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

    // === Создание заказа без авторизации: отдельные тесты ===

    @Test
    @DisplayName("Статус 401 при создании заказа без авторизации")
    @Description("Проверка, что неавторизованный запрос возвращает 401")
    public void checkStatusCodeOnCreateOrderWithoutAuth() {
        OrderRequest orderRequest = new OrderRequest(ingredients);
        apiClient.createOrder(orderRequest, null).statusCode(STATUS_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Поле success=false при создании заказа без авторизации")
    @Description("Проверка значения success для неавторизованного запроса")
    public void checkSuccessFieldOnCreateOrderWithoutAuth() {
        OrderRequest orderRequest = new OrderRequest(ingredients);
        apiClient.createOrder(orderRequest, null).body("success", equalTo(false));
    }

    @Test
    @DisplayName("Сообщение об ошибке при создании заказа без авторизации")
    @Description("Проверка текста сообщения для неавторизованного запроса")
    public void checkErrorMessageOnCreateOrderWithoutAuth() {
        OrderRequest orderRequest = new OrderRequest(ingredients);
        apiClient.createOrder(orderRequest, null)
                .body("message", containsString("You should be authorised"));
    }

    // === Другие сценарии: разбиваем на отдельные проверки ===

    @Test
    @DisplayName("Статус 200 при создании заказа с одним ингредиентом")
    @Description("Проверка статуса для заказа с минимально допустимым количеством ингредиентов")
    public void checkStatusCodeWithOneIngredient() {
        List<String> oneIngredient = getRandomIngredients(1);
        OrderRequest orderRequest = new OrderRequest(oneIngredient);
        apiClient.createOrder(orderRequest, token).statusCode(STATUS_OK);
    }

    @Test
    @DisplayName("Статус 400 при создании заказа без ингредиентов")
    @Description("Проверка статуса для пустого массива ингредиентов")
    public void checkStatusCodeWithoutIngredients() {
        OrderRequest emptyOrder = new OrderRequest(List.of());
        apiClient.createOrder(emptyOrder, token).statusCode(STATUS_BAD_REQUEST);
    }

    @Test
    @DisplayName("Поле success=false и сообщение об ошибке при создании заказа без ингредиентов")
    @Description("Проверка success и сообщения для пустого массива ингредиентов")
    public void checkErrorResponseWithoutIngredients() {
        OrderRequest emptyOrder = new OrderRequest(List.of());
        apiClient.createOrder(emptyOrder, token)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Статус 500 при создании заказа с некорректными ингредиентами")
    @Description("Проверка статуса для некорректных данных ингредиентов")
    public void checkStatusCodeWithInvalidIngredients() {
        List<String> invalidIngredients = List.of("invalidHash1", "invalidHash2");
        OrderRequest orderRequest = new OrderRequest(invalidIngredients);
        apiClient.createOrder(orderRequest, token).statusCode(STATUS_INTERNAL_ERROR);
    }
}
