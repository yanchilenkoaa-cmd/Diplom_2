import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BaseTest {

    protected static final String BASE_URL = "https://stellarburgers.education-services.ru";  // поменяйте на реальный
    protected static final int STATUS_OK = 200;
    protected static final int STATUS_BAD_REQUEST = 400;
    protected static final int STATUS_UNAUTHORIZED = 401;
    protected static final int STATUS_FORBIDDEN = 403;
    protected static final int STATUS_INTERNAL_ERROR = 500;

    protected RequestSpecification requestSpec;
    protected UserClient userClient;

    @Before
    public void setUp() {
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType("application/json")
                .build();

        userClient = new UserClient(requestSpec);
    }

    protected String generateUniqueEmail() {
        return "user" + System.currentTimeMillis() + "@test.com";
    }
    protected List<String> getRandomIngredients(int count) {
        List<String> allIngredients = List.of(
                "ingredientHash1",
                "ingredientHash2",
                "ingredientHash3",
                "ingredientHash4",
                "ingredientHash5"
        );

        List<String> selected = new ArrayList<>();
        int max = allIngredients.size();

        while (selected.size() < count && selected.size() < max) {
            int idx = ThreadLocalRandom.current().nextInt(max);
            String ingredient = allIngredients.get(idx);
            if (!selected.contains(ingredient)) {
                selected.add(ingredient);
            }
        }
        return selected;
    }
}