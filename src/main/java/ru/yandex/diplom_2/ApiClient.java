package ru.yandex.diplom_2;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import com.github.javafaker.Faker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import static io.restassured.RestAssured.given;

import com.google.gson.JsonObject;


public class ApiClient {

    //  URL для API
    public static final String BASE_URL = "https://stellarburgers.nomoreparties.site/api";
    private static final String ENDPOINT_INGREDIENTS = "/ingredients";
    private static final String ENDPOINT_CREATE_ORDER = "/orders";
    private static final String ENDPOINT_REGISTER_USER = "/auth/register";
    private static final String ENDPOINT_LOGIN_USER = "/auth/login";
    public static final String ENDPOINT_UPDATE_USER_PROFILE = "/auth/user";
    private static final String ENDPOINT_DELETE_USER = "/auth/user";
    private static final String ENDPOINT_GET_USER_ORDERS = "/orders";


    // Методы для получения ингредиентов
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    public static Response getIngredients() {
        return given()
                .contentType("application/json")
                .get(BASE_URL + ENDPOINT_INGREDIENTS);
    }

    public static String getRandomIngredientId() {
        List<String> ingredientIds = getIngredientIds();
        if (ingredientIds.isEmpty()) {
            throw new IllegalStateException("No ingredient IDs available.");
        }
        Random random = new Random();
        int randomIndex = random.nextInt(ingredientIds.size());
        return ingredientIds.get(randomIndex);
    }

    public static List<String> getIngredientIds() {
        Response response = getIngredients();
        List<String> ingredientIds = new ArrayList<>();

        if (response.getStatusCode() == 200) {
            JsonObject responseObject = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
            JsonArray dataArray = responseObject.getAsJsonArray("data");

            for (JsonElement element : dataArray) {
                String ingredientId = element.getAsJsonObject().get("_id").getAsString();
                ingredientIds.add(ingredientId);
            }
        }

        return ingredientIds;
    }

    private static String ingredientsToJsonArray(String[] ingredients) {
        StringBuilder jsonArray = new StringBuilder("[");
        for (int i = 0; i < ingredients.length; i++) {
            jsonArray.append("\"").append(ingredients[i]).append("\"");
            if (i < ingredients.length - 1) {
                jsonArray.append(",");
            }
        }
        jsonArray.append("]");
        return jsonArray.toString();
    }
    

    // Методы для создания заказа

    public static Response createOrderWithRandomIngredientsWithoutToken() {
        String randomIngredientId = getRandomIngredientId();
        String[] ingredients = {randomIngredientId};
        return createOrder(ingredients, ""); // Пустой AccessToken
    }

    public static Response createOrderWithoutIngredients(String accessToken) {
        String requestBody = "{\"ingredients\": []}"; // Пустой массив ингредиентов
        return post(ENDPOINT_CREATE_ORDER, requestBody, accessToken);
    }


     public static Response createOrder(String[] ingredients, String accessToken) {
        String requestBody = "{\"ingredients\":" + ingredientsToJsonArray(ingredients) + "}";
        return post(ENDPOINT_CREATE_ORDER, requestBody, accessToken);
    }

    public static Response createOrderWithInvalidHash(String accessToken) {
        String randomIngredientId1 = ApiClient.getRandomIngredientId();
        String randomIngredientId2 = ApiClient.getRandomIngredientId();

        // Изменяем id ингредиентов, добавляя "invalid-" в начало каждого id
        String modifiedIngredientId1 = "invalid-" + randomIngredientId1;
        String modifiedIngredientId2 = "invalid-" + randomIngredientId2;

        String[] ingredients = {modifiedIngredientId1, modifiedIngredientId2};

        String requestBody = "{\"ingredients\":" + ingredientsToJsonArray(ingredients) + "}";
        return post(ENDPOINT_CREATE_ORDER, requestBody, accessToken);
    }

    public static Response createOrderWithRandomIngredients(String accessToken) {

        String randomIngredientId = getRandomIngredientId();
        String[] ingredients = {randomIngredientId};
        return createOrder(ingredients, accessToken);
    }


    // Методы для взаимодействия с пользователем

    public static Response registerUser(String email, String userPassword, String name) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("email", email);
        requestBody.addProperty("password", userPassword);
        requestBody.addProperty("name", name);

        Response response = post(ENDPOINT_REGISTER_USER, requestBody.toString()); // Передаем JSON-строку

        return response;
    }

    public static Response loginUser(String email, String userPassword) {
        String requestBody = "{\"email\":\"" + email + "\", \"password\":\"" + userPassword + "\"}";
        return post(ENDPOINT_LOGIN_USER, requestBody);
    }


    public static Response updateUserProfile(String newName, String newEmail, String accessToken) {
        String requestBody = "{\"name\":\"" + newName + "\", \"email\":\"" + newEmail + "\"}";
        return patchWithJsonBody(ENDPOINT_UPDATE_USER_PROFILE, requestBody, accessToken);
    }


    public static Response deleteUser(String accessToken) {
        return deleteWithToken(ENDPOINT_DELETE_USER, accessToken);
    }


   public static Response getUserOrders(String accessToken) {
       return getWithToken(ENDPOINT_GET_USER_ORDERS, accessToken);
    }


    // Методы для создания уникального пользователя

    private static String userPassword = "123QWE";
    public static String getUserPassword() {
        return userPassword;
    }

    public static String generateRandomEmail() {
        Faker faker = new Faker();
        return faker.internet().emailAddress();
    }

    public static String generateRandomName() {
        Faker faker = new Faker();
        return faker.name().fullName();
    }


    // Методы для запросов

    private static Response post(String endpoint, String requestBody) {
        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(endpoint);
    }

    private static Response post(String endpoint, String requestBody, String accessToken) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .body(requestBody)
                .when()
                .post(endpoint);
    }

    public static Response patchWithJsonBody(String endpoint, String requestBody, String accessToken) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .body(requestBody)
                .when()
                .patch(endpoint);
    }

    private static Response deleteWithToken(String endpoint, String accessToken) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .when()
                .delete(endpoint);
    }

    private static Response getWithToken(String endpoint, String accessToken) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .when()
                .get(endpoint);
    }



}
