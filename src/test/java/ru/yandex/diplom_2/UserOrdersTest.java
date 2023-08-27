package ru.yandex.diplom_2;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.restassured.response.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;

@Epic("Diplom_2")
@Feature("User Orders")
public class UserOrdersTest {

    private String createUserAccessToken;
    private String loginUserAccessToken;
    private int createdOrderNumber; // Изменили тип данных на int

    @Before
    public void setUp() {
        ApiClient.setup();

        // Создание пользователя и получение AccessToken
        String userEmail = ApiClient.generateRandomEmail();
        String userName = ApiClient.generateRandomName();
        String userPassword = ApiClient.getUserPassword();
        Response createUserResponse = ApiClient.registerUser(userEmail, userPassword, userName);
        assertEquals(200, createUserResponse.statusCode());

        createUserAccessToken = createUserResponse.jsonPath().getString("accessToken");
        System.out.println("User created:");
        System.out.println("Email: " + userEmail);
        System.out.println("Name: " + userName);
        System.out.println("Password: " + userPassword);

        // Логин пользователя и получение нового AccessToken
        Response loginResponse = ApiClient.loginUser(userEmail, userPassword);
        assertEquals(200, loginResponse.statusCode());
        loginUserAccessToken = loginResponse.jsonPath().getString("accessToken");
        System.out.println("\nUser logged in:");
        System.out.println("User's AccessToken after login: " + loginUserAccessToken);

        // Создаем заказ с рандомными ингредиентами и сохраняем номер заказа
        Response createOrderResponse = ApiClient.createOrderWithRandomIngredients(loginUserAccessToken);
        createOrderResponse.then().statusCode(200);
        createdOrderNumber = createOrderResponse.jsonPath().getInt("order.number"); // Преобразование в числовой формат
    }

    @Test
    @DisplayName("Test getting user orders with authorization")
    @Description("Test for getting user orders with authorization")

    public void testGetUserOrdersWithAuthorization() {
        // Получаем заказы пользователя с использованием авторизованного токена
        Response getUserOrdersResponse = ApiClient.getUserOrders(loginUserAccessToken);
        getUserOrdersResponse.then().log().all();

        // Проверяем, что в полученных заказах есть созданный заказ
        getUserOrdersResponse.then().statusCode(200)
                .body("success", equalTo(true))
                .body("orders[0].number", equalTo(createdOrderNumber)); // Не нужно преобразование, так как createdOrderNumber уже целое число
    }

    @Test
    @DisplayName("Test getting user orders without authorization")
    @Description("Test for getting user orders without authorization")

    public void testGetUserOrdersWithoutAuthorization() {
        // Попытка получить заказы без авторизации (ожидаем статус 401)
        Response getUserOrdersResponse = ApiClient.getUserOrders("");

        // Проверяем ожидаемый статус и содержимое ответа
        getUserOrdersResponse.then().statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @After
    public void tearDown() {
        // Удаляем пользователя после завершения теста
        ApiClient.deleteUser(createUserAccessToken);
        System.out.println("User with email has been deleted.");
    }
}
