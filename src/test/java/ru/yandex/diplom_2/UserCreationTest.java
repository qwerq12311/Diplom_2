package ru.yandex.diplom_2;

import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.restassured.response.Response;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static io.restassured.RestAssured.*;

@Epic("Diplom_2")
@Feature("User Creation")

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
    @DisplayName("Тест на создание уникального пользователя")
    @Description("Тест на создание уникального пользователя")

    public void testCreateUniqueUser() {
        // Генерируем случайные данные для создания пользователя
        String email = apiClient.generateRandomEmail();
        String password = apiClient.getUserPassword();
        String name = apiClient.generateRandomName();

        // Создаем пользователя
        Response response = apiClient.registerUser(email, password, name);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true));

        // Сохраняем данные созданного пользователя для дальнейшего использования
        createdUserAccessToken = response.body().jsonPath().getString("accessToken");


        assertTrue(response.path("success"));
        System.out.println("Тест создания уникального пользователя завершен успешно");
    }

    @Test
    @DisplayName("Тест на создание пользователя с дублированным email")
    @Description("Тест на создание пользователя с дублированным email")

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
    @DisplayName("Тест на создание пользователя с незаполненными обязательными полями")
    @Description("Тест на создание пользователя с незаполненными обязательными полями")

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
