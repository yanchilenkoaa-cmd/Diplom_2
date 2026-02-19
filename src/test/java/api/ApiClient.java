package api;

import io.qameta.allure.Step;
import io.restassured.specification.RequestSpecification;
import io.restassured.response.ValidatableResponse;
import models.OrderRequest;
import models.UserCredentials;

import static io.restassured.RestAssured.given;

public class ApiClient {
    protected static final String ENDPOINT_REGISTER = "/api/auth/register";
    protected static final String ENDPOINT_LOGIN = "/api/auth/login";
    protected static final String ENDPOINT_INGREDIENTS = "/api/ingredients";
    protected static final String ENDPOINT_ORDERS = "/api/orders";

    private final RequestSpecification requestSpec;

    public ApiClient(RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;

    }
    @Step("Регистрация нового пользователя")
    public ValidatableResponse registerUser(UserCredentials credentials) {
        return given()
                .spec(requestSpec)
                .body(credentials)
                .when()
                .post(ENDPOINT_REGISTER)
                .then();
    }

    @Step("Авторизация пользователя")
    public ValidatableResponse loginUser(UserCredentials credentials) {
        return given()
                .spec(requestSpec)
                .body(credentials)
                .when()
                .post(ENDPOINT_LOGIN)
                .then();
    }

    @Step("Получение списка ингредиентов")
    public ValidatableResponse getIngredients() {
        return given()
                .spec(requestSpec)
                .when()
                .get(ENDPOINT_INGREDIENTS)
                .then();
    }


    @Step("Создание заказа")
    public ValidatableResponse createOrder(OrderRequest orderRequest, String token) {
        RequestSpecification spec = given().spec(requestSpec).body(orderRequest);

        if (token != null && !token.trim().isEmpty()) {
            if (token.startsWith("Bearer ")) {
                spec.header("Authorization", token); // используем как есть
            } else {
                spec.header("Authorization", "Bearer " + token); // добавляем префикс
            }
        }

        return spec.when().post(ENDPOINT_ORDERS).then();
    }

    @Step("Получение ингредиентов с авторизацией")
    public ValidatableResponse getIngredients(String token) {
        RequestSpecification spec = given().spec(requestSpec);

        if (token != null && !token.trim().isEmpty()) {
            if (token.startsWith("Bearer ")) {
                spec.header("Authorization", token);
            } else {
                spec.header("Authorization", "Bearer " + token);
            }
        }

        return spec.when().get(ENDPOINT_INGREDIENTS).then();
    }
}
