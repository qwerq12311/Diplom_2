import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.github.javafaker.Faker;
import io.restassured.response.Response;
import ru.yandex.diplom_2.ApiClient;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;

public class UserProfileUpdateTest {

    private String accessToken;
    private String userEmail;

    @Before
    public void setUp() {
        ApiClient.setup();

        // Создание пользователя и получение AccessToken
        userEmail = ApiClient.generateRandomEmail();
        String userName = ApiClient.generateRandomName();
        String userPassword = ApiClient.getUserPassword();
        Response createUserResponse = ApiClient.registerUser(userEmail, userPassword, userName);
        assertEquals(200, createUserResponse.statusCode());

        accessToken = createUserResponse.jsonPath().getString("accessToken");
        System.out.println("User created:");
        System.out.println("Email: " + userEmail);
        System.out.println("Name: " + userName);
        System.out.println("Password: " + userPassword);

        // Логин пользователя и получение нового AccessToken
        Response loginResponse = ApiClient.loginUser(userEmail, userPassword);
        assertEquals(200, loginResponse.statusCode());
        accessToken = loginResponse.jsonPath().getString("accessToken");
        System.out.println("\nUser logged in:");
        System.out.println("User's AccessToken after login: " + accessToken);
    }

    @Test
    public void testUpdateUserProfileWithAuthorization() {
        // Генерируем новые случайные данные
        Faker faker = new Faker();
        String newEmail = faker.internet().safeEmailAddress();
        String newName = faker.name().fullName();

        // Вызываем API для обновления профиля с access token и новыми данными
        Response updateProfileResponse = ApiClient.updateUserProfile(newName, newEmail, accessToken);

        updateProfileResponse.then().log().all();

        // Проверяем ожидаемый статус и содержимое ответа
        updateProfileResponse.then().statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail))
                .body("user.name", equalTo(newName));
    }

    @Test
    public void testUpdateUserProfileWithoutAuthorization() {
        // Генерируем новые случайные данные
        Faker faker = new Faker();
        String newEmail = faker.internet().safeEmailAddress();
        String newName = faker.name().fullName();

        // Вызываем API для обновления профиля без access token (ожидаем статус 401)
        Response updateProfileResponse = ApiClient.updateUserProfile(newName, newEmail, "");

        updateProfileResponse.then().log().all();

        // Проверяем ожидаемый статус и содержимое ответа
        int expectedStatusCode = 401;
        updateProfileResponse.then().statusCode(expectedStatusCode)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));

        if (updateProfileResponse.getStatusCode() != expectedStatusCode) {
            System.out.println("Actual status code: " + updateProfileResponse.getStatusCode());
            System.out.println("Response body: " + updateProfileResponse.getBody().asString());
        }
    }

    @After
    public void tearDown() {
        // Удаляем пользователя после завершения теста
        ApiClient.deleteUser(accessToken);
        System.out.println("User with email " + userEmail + " has been deleted.");
    }
}
