package ru.yandex.diplom_2;

import com.github.javafaker.Faker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.restassured.response.Response;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static io.restassured.RestAssured.*;

public class UserLoginTest {

    private ApiClient apiClient;
    private Faker faker;

    private String createdUserEmail;
    private String createdUserName;
    private String createdUserPassword;
    private String createdUserAccessToken;

    @Before
    public void setup() {
        ApiClient.setup();
        apiClient = new ApiClient();
        faker = new Faker();


        // Создаем уникального пользователя для тестов
        createdUserEmail = faker.internet().emailAddress();
        createdUserPassword = faker.internet().password();
        createdUserName = faker.name().fullName();
        Response response = apiClient.registerUser(createdUserEmail, createdUserPassword, createdUserName);
        createdUserAccessToken = response.body().jsonPath().getString("accessToken");
    }

    @After
    public void tearDown() {
        // Удаляем созданного пользователя
        if (createdUserAccessToken != null) {
            apiClient.deleteUser(createdUserAccessToken);
        }
    }

    @Test
    public void testSuccessfulUserLogin() {
        Response response = apiClient.loginUser(createdUserEmail, createdUserPassword);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(createdUserEmail))
                .body("user.name", equalTo(createdUserName))
                .body("accessToken", startsWith("Bearer"));

        String successValue = response.jsonPath().getString("success"); // Получаем значение success из JSON как строку
        assertEquals("true", successValue); // Проверяем, что success равен "true"


        System.out.println("testSuccessfulUserLogin: Test passed");
    }

    @Test
    public void testUnsuccessfulUserLogin() {
        Response response = apiClient.loginUser("invalidEmail", "invalidToken");

        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect")); // Изменено ожидаемое сообщение

        String message = response.jsonPath().getString("message");
        assertEquals("email or password are incorrect", message);

        System.out.println("testUnsuccessfulUserLogin: Test passed");
    }
}
