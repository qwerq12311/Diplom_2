package ru.yandex.diplom_2;

import com.github.javafaker.Faker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.restassured.response.Response;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static io.restassured.RestAssured.*;

public class UserCreationTest {

    private ApiClient apiClient;
    private Faker faker;

    private String createdUserAccessToken;

    @Before
    public void setup() {
        ApiClient.setup(); // Вызываем метод из ApiClient для установки базового URL
        apiClient = new ApiClient();
        faker = new Faker();
    }

    @After
    public void tearDown() {
        if (createdUserAccessToken != null) {
            apiClient.deleteUser(createdUserAccessToken);
        }
    }

    @Test
    public void testCreateUniqueUser() {
        Response response = apiClient.createUniqueUser();

        response.then()
                .statusCode(200)
                .body("success", equalTo(true));

        createdUserAccessToken = response.body().jsonPath().getString("accessToken");

        assertThat(response.path("success"), equalTo(true));
        System.out.println("Тест создания уникального пользователя завершен успешно");
    }

    @Test
    public void testCreateDuplicateUser() {
        String email = faker.internet().emailAddress();
        String password = faker.internet().password();
        String name = faker.name().fullName();

        // First, create the user
        apiClient.registerUser(email, password, name);

        // Then, try creating the user with the same email again
        Response response = apiClient.registerUser(email, password, name);

        response.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));

        assertThat(response.path("success"), equalTo(false));
        assertThat(response.path("message"), equalTo("User already exists"));
        System.out.println("Тест создания пользователя с уже существующим email завершен успешно");
    }

    @Test
    public void testCreateUserWithMissingFields() {
        Response response = apiClient.registerUser("", "", "");

        response.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));

        assertThat(response.path("success"), equalTo(false));
        assertThat(response.path("message"), equalTo("Email, password and name are required fields"));
        System.out.println("Тест создания пользователя с незаполненными обязательными полями завершен успешно");
    }
}

