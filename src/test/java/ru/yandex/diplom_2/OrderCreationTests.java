package ru.yandex.diplom_2;

import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.restassured.path.json.JsonPath;


import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class OrderCreationTests {

    private String createUserAccessToken;
    private String loginUserAccessToken;
    private String userEmail;
    private String userName;
    private String userPassword;

    @Before
    public void setUp() {
        ApiClient.setup();

        // Создание пользователя и получение AccessToken
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
    public void testCreateOrderWithAuthenticationAndIngredients() {
        List<String> ingredientIds = ApiClient.getIngredientIds();
        String randomIngredientId = ApiClient.getRandomIngredientId();
        String[] ingredients = {randomIngredientId};

        Response orderResponse = ApiClient.createOrder(ingredients, loginUserAccessToken);

        System.out.println("Response Status Code: " + orderResponse.statusCode());
        System.out.println("Response Body: " + orderResponse.body().asString());
        assertEquals(200, orderResponse.statusCode());
    }

    @Test
    public void testCreateOrderWithoutAuthentication() {
        String randomIngredientId = ApiClient.getRandomIngredientId();
        String[] ingredients = {randomIngredientId};

        Response orderResponse = ApiClient.createOrderWithRandomIngredientsWithoutToken();

        System.out.println("Response Status Code: " + orderResponse.statusCode());
        System.out.println("Response Body: " + orderResponse.body().asString());
        assertEquals(200, orderResponse.statusCode()); // Ожидаем статус 200 , и пример Response Body: {"success":true,"name":"Фалленианский краторный бургер","order":{"number":5866}}
    }

    @Test
    public void testCreateOrderWithIngredients() {
        List<String> ingredientIds = ApiClient.getIngredientIds();
        String randomIngredientId = ApiClient.getRandomIngredientId();
        String[] ingredients = {randomIngredientId};

        Response orderResponse = ApiClient.createOrder(ingredients, loginUserAccessToken);

        System.out.println("Response Status Code: " + orderResponse.statusCode());
        System.out.println("Response Body: " + orderResponse.body().asString());
        assertEquals(200, orderResponse.statusCode());
    }

    @Test
    public void testCreateOrderWithoutIngredients() {
        List<String> emptyIngredientsList = new ArrayList<>(); // Создаем пустой список ингредиентов

        Response orderResponse = ApiClient.createOrder(emptyIngredientsList.toArray(new String[0]), loginUserAccessToken);

        System.out.println("Response Status Code: " + orderResponse.statusCode());
        System.out.println("Response Body: " + orderResponse.body().asString());
        assertEquals(400, orderResponse.statusCode()); // Ожидаем статус 400 Bad Request
    }

    @Test
    public void testCreateOrderWithInvalidIngredientHash() {
        String randomIngredientId1 = ApiClient.getRandomIngredientId();
        String randomIngredientId2 = ApiClient.getRandomIngredientId();

        // Изменяем id ингредиентов, добавляя "-invalid" в конец каждого id
        String modifiedIngredientId1 = randomIngredientId1 + "-invalid";
        String modifiedIngredientId2 = randomIngredientId2 + "-invalid";

        String[] ingredients = {modifiedIngredientId1, modifiedIngredientId2};

        Response orderResponse = ApiClient.createOrder(ingredients, loginUserAccessToken);

        System.out.println("Response Status Code: " + orderResponse.statusCode());
        System.out.println("Response Body: " + orderResponse.body().asString());
        assertEquals(400, orderResponse.statusCode()); // Ожидаем статус 400 Bad Request
    }
}