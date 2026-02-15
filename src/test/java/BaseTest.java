import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import org.junit.BeforeClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class BaseTest {

    // Константы эндпоинтов (без дублирования /api)
    protected static final String ENDPOINT_REGISTER = "/auth/register";
    protected static final String ENDPOINT_LOGIN = "/auth/login";
    protected static final String ENDPOINT_INGREDIENTS = "/ingredients";
    protected static final String ENDPOINT_ORDERS = "/orders";

    // Коды статусов
    protected static final int STATUS_OK = 200;
    protected static final int STATUS_UNAUTHORIZED = 401;
    protected static final int STATUS_BAD_REQUEST = 400;
    protected static final int STATUS_FORBIDDEN = 403;
    protected static final int STATUS_INTERNAL_ERROR = 500;

    // Заголовки
    //protected static final String HEADER_CONTENT_TYPE = "Content-Type";
    protected static final String HEADER_AUTHORIZATION = "Authorization";

    // Типы контента
    protected static final String CONTENT_TYPE_JSON = "application/json";

    protected static RequestSpecification requestSpec;

    @BeforeClass
    public static void setup() {
        // Исправленный базовый URL (убрана лишняя косая черта в протоколе)
        RestAssured.baseURI = "https://stellarburgers.education-services.ru";
        // Убрано дублирование /api в basePath
        RestAssured.basePath = "";

        requestSpec = new RequestSpecBuilder()
                .setContentType(CONTENT_TYPE_JSON)
                .log(LogDetail.ALL)
                .build();
    }

    /** Получить токен авторизации для пользователя */
    protected String loginAndGetToken(String email, String password) {
        return given()
                .spec(requestSpec)
                .body(String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password))
                .when()
                .post(ENDPOINT_LOGIN)
                .then()
                .log().ifError() // логируем тело ответа при ошибке
                .statusCode(STATUS_OK)
                .extract()
                .path("accessToken");
    }

    /** Зарегистрировать пользователя и получить токен */
    protected String registerAndGetToken(String email, String password, String name) {
        given()
                .spec(requestSpec)
                .body(String.format("{\"email\":\"%s\", \"password\":\"%s\", \"name\":\"%s\"}",
                        email, password, name))
                .when()
                .post(ENDPOINT_REGISTER)
                .then()
                .log().ifError()
                .statusCode(STATUS_OK);

        return loginAndGetToken(email, password);
    }

    /** Получить список ингредиентов */
    protected List<String> getIngredients() {
        try {
            return given()
                    .spec(requestSpec)
                    .when()
                    .get(ENDPOINT_INGREDIENTS)
                    .then()
                    .log().ifError()
                    .statusCode(STATUS_OK)
                    .extract()
                    .jsonPath() // явное указание на JSONPath
                    .getList("data._id"); // предполагаем структуру {"data": [{"_id": "..." }]}
        } catch (Exception e) {
            throw new RuntimeException("Не удалось получить список ингредиентов: " + e.getMessage(), e);
        }
    }

    /** Выбрать случайные ингредиенты */
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

    /** Сгенерировать уникальный email */
    protected String generateUniqueEmail() {
        return "test" + System.currentTimeMillis() + "@yandex.ru";
    }

    /** Создать тело запроса для заказа */
    protected String createOrderBody(List<String> ingredientIds) {
        String ingredientsJson = ingredientIds.stream()
                .map(id -> "\"" + id + "\"")
                .collect(Collectors.joining(", ", "[", "]"));
        return String.format("{\"ingredients\": %s}", ingredientsJson);
    }
}
