package ru.yandex.diplom_2;

import io.qameta.allure.*;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.restassured.path.json.JsonPath;

import java.util.List;

import static org.junit.Assert.assertEquals;


@Epic("Diplom_2")
@Feature("Order creation")

public class OrderCreationTests {

    private String createUserAccessToken;
    private String loginUserAccessToken;
    private String userEmail;
    private String userName;
    private String userPassword;

    @Before
    public void setUp() {
        ApiClient.setup();


        userEmail = ApiClient.generateRandomEmail();
        userName = ApiClient.generateRandomName();
        userPassword = ApiClient.getUserPassword();
        Response createUserResponse = ApiClient.registerUser(userEmail, userPassword, userName);
        assertEquals(200, createUserResponse.statusCode());

        JsonPath createUserJsonPath = createUserResponse.jsonPath();
        createUserAccessToken = createUserJsonPath.getString("accessToken");
        System.out.println("User created:");
        System.out.println("Email: " + userEmail);
        System.out.println("Name: " + userName);
        System.out.println("Password: " + userPassword);

        // Логин пользователя и получение нового AccessToken
        Response loginResponse = ApiClient.loginUser(userEmail, userPassword);
        assertEquals(200, loginResponse.statusCode());

        JsonPath loginJsonPath = loginResponse.jsonPath();
        loginUserAccessToken = loginJsonPath.getString("accessToken");
        System.out.println("\nUser logged in:");
        System.out.println("User's AccessToken after login: " + loginUserAccessToken);
    }

    @After
    public void tearDown() {
        System.out.println("Deleting User with AccessToken: " + createUserAccessToken);
        ApiClient.deleteUser(createUserAccessToken);
    }

    @Test
    @DisplayName("Тест создания заказа с аутентификацией и ингредиентами")
    @Description("Выполняем тест на создание заказа с аутентификацией и ингредиентами")

    public void testCreateOrderWithAuthenticationAndIngredients() {

        String randomIngredientId = ApiClient.getRandomIngredientId();
        String[] ingredients = {randomIngredientId};

        Response orderResponse = ApiClient.createOrder(ingredients, loginUserAccessToken);

        System.out.println("Response Status Code: " + orderResponse.statusCode());
        System.out.println("Response Body: " + orderResponse.body().asString());
        assertEquals(200, orderResponse.statusCode());

        // информация о созданном заказе из ответа
        JsonPath orderJson = orderResponse.jsonPath();
        String orderId = orderJson.getString("order.number");
        String orderStatus = orderJson.getString("order.status");
        List<String> orderIngredients = orderJson.getList("order.ingredients");

        // информация о заказе и его ингредиентах
        System.out.println("Created Order number: " + orderId);
        System.out.println("Order Status: " + orderStatus);
        System.out.println("Order Ingredients: " + orderIngredients);
    }

    @Test
    @DisplayName("Тест создания заказа без аутентификации")
    @Description("Выполняем тест на создание заказа без аутентификации")

    public void testCreateOrderWithoutAuthentication() {
        Response orderResponse = ApiClient.createOrderWithRandomIngredientsWithoutToken();
        System.out.println("Response Status Code: " + orderResponse.statusCode());
        System.out.println("Response Body: " + orderResponse.body().asString());
        assertEquals(200, orderResponse.statusCode()); // Ожидаем статус 200
    }

    @Test
    @DisplayName("Тест создания заказа с ингредиентами")
    @Description("Выполняем тест на создание заказа с ингредиентами")

    public void testCreateOrderWithIngredients() {
        Response orderResponse = ApiClient.createOrderWithRandomIngredients(loginUserAccessToken);
        System.out.println("Response Status Code: " + orderResponse.statusCode());
        System.out.println("Response Body: " + orderResponse.body().asString());
        assertEquals(200, orderResponse.statusCode());

    }

    @Test
    @DisplayName("Тест создания заказа без ингредиентов")
    @Description("Выполняем тест на создание заказа без ингредиентов")

    public void testCreateOrderWithoutIngredients() {

        Response orderResponse = ApiClient.createOrderWithoutIngredients(loginUserAccessToken);

        System.out.println("Response Status Code: " + orderResponse.statusCode());
        System.out.println("Response Body: " + orderResponse.body().asString());
        assertEquals(400, orderResponse.statusCode()); // Ожидаем статус 400 Bad Request
    }

    @Test
    @DisplayName("Тест создания заказа с некорректным хешем ингредиента")
    @Description("Выполняем тест на создание заказа с некорректным хешем ингредиента")

    public void testCreateOrderWithInvalidIngredientHash() {



        Response orderResponse = ApiClient.createOrderWithInvalidHash(loginUserAccessToken);

        System.out.println("Response Status Code: " + orderResponse.statusCode());
        System.out.println("Response Body: " + orderResponse.body().asString());
        assertEquals(500, orderResponse.statusCode()); // Ожидаем статус 400 Bad Request
    }

}