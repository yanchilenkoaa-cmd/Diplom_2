package base;

import api.ApiClient;
import io.qameta.allure.Step;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import io.restassured.builder.RequestSpecBuilder;
import models.UserCredentials;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BaseTest {

    // Коды статусов
    protected static final int STATUS_OK = 200;
    protected static final int STATUS_UNAUTHORIZED = 401;
    protected static final int STATUS_BAD_REQUEST = 400;
    protected static final int STATUS_FORBIDDEN = 403;
    protected static final int STATUS_INTERNAL_ERROR = 500;

    // Заголовки
    protected static final String HEADER_AUTHORIZATION = "Authorization";

    // Типы контента
    protected static final String CONTENT_TYPE_JSON = "application/json";

    protected RequestSpecification requestSpec;
    protected ApiClient apiClient;

    @Before
    public void setup() {
        io.restassured.RestAssured.baseURI = "https://stellarburgers.education-services.ru";
        io.restassured.RestAssured.basePath = "";

        requestSpec = new RequestSpecBuilder()
                .setContentType(CONTENT_TYPE_JSON)
                .log(LogDetail.ALL)
                .build();

        apiClient = new ApiClient(requestSpec);
    }

    @Step("Получение токена авторизации для пользователя {email}")
    protected String loginAndGetToken(String email, String password) {
        UserCredentials credentials = new UserCredentials(email, password);
        return apiClient.loginUser(credentials)
                .log().ifError()
                .statusCode(STATUS_OK)
                .extract()
                .path("accessToken");
    }

    @Step("Регистрация пользователя {email} и получение токена")
    protected String registerAndGetToken(String email, String password, String name) {
        UserCredentials credentials = new UserCredentials(email, password, name);
        apiClient.registerUser(credentials)
                .log().ifError()
                .statusCode(STATUS_OK);

        return loginAndGetToken(email, password);
    }

    @Step("Получение списка ингредиентов")
    protected List<String> getIngredients() {
        try {
            return apiClient.getIngredients()
                    .log().ifError()
                    .statusCode(STATUS_OK)
                    .extract()
                    .jsonPath()
                    .getList("data._id");
        } catch (Exception e) {
            throw new RuntimeException("Не удалось получить список ингредиентов: " + e.getMessage(), e);
        }
    }

    protected List<String> getRandomIngredients(int count) {
        List<String> allIngredients = getIngredients();
        if (allIngredients.size() < count) {
            throw new RuntimeException(String.format(
                    "Недостаточно ингредиентов для выбора. Доступно: %d, требуется: %d",
                    allIngredients.size(), count));
        }

        List<String> shuffled = new ArrayList<>(allIngredients);
        Collections.shuffle(shuffled);
        return shuffled.stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    protected String generateUniqueEmail() {
        return "test" + System.currentTimeMillis() + "@yandex.ru";
    }
}
