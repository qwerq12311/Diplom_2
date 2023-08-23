package ru.yandex.diplom_2;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ApiClient {

    public static final String BASE_URL = "https://stellarburgers.nomoreparties.site/api";
    private static final String ENDPOINT_INGREDIENTS = "/ingredients";
    private static final String ENDPOINT_CREATE_ORDER = "/orders";
    private static final String ENDPOINT_RESET_PASSWORD = "/password-reset";
    private static final String ENDPOINT_RESET_PASSWORD_RESET = "/password-reset/reset";
    private static final String ENDPOINT_REGISTER_USER = "/auth/register";
    private static final String ENDPOINT_LOGIN_USER = "/auth/login";
    private static final String ENDPOINT_LOGOUT_USER = "/auth/logout";
    private static final String ENDPOINT_REFRESH_TOKEN = "/auth/token";
    private static final String ENDPOINT_GET_USER_PROFILE = "/auth/user";
    private static final String ENDPOINT_UPDATE_USER_PROFILE = "/auth/user";
    private static final String ENDPOINT_DELETE_USER = "/auth/user";
    private static final String ENDPOINT_GET_ALL_ORDERS = "/orders/all";
    private static final String ENDPOINT_GET_USER_ORDERS = "/orders";

    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    public static Response getIngredients() {
        return get(ENDPOINT_INGREDIENTS);
    }

    public static Response createOrder(String[] ingredients, String token) {
        String requestBody = "{\"ingredients\":" + ingredientsToJsonArray(ingredients) + "}";
        return post(ENDPOINT_CREATE_ORDER, requestBody, token);
    }

    public static Response resetUserPassword(String email) {
        String requestBody = "{\"email\":\"" + email + "\"}";
        return post(ENDPOINT_RESET_PASSWORD, requestBody);
    }

    public static Response resetUserPasswordWithToken(String newPassword, String resetToken) {
        String requestBody = "{\"password\":\"" + newPassword + "\", \"token\":\"" + resetToken + "\"}";
        return post(ENDPOINT_RESET_PASSWORD_RESET, requestBody);
    }

    public static Response registerUser(String email, String password, String name) {
        String requestBody = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\", \"name\":\"" + name + "\"}";
        return post(ENDPOINT_REGISTER_USER, requestBody);
    }

    public static Response loginUser(String email, String password) {
        String requestBody = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}";
        return post(ENDPOINT_LOGIN_USER, requestBody);
    }

    public static Response logoutUser(String refreshToken) {
        String requestBody = "{\"token\":\"" + refreshToken + "\"}";
        return post(ENDPOINT_LOGOUT_USER, requestBody);
    }

    public static Response refreshToken(String email, String password) {
        String requestBody = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}";
        return post(ENDPOINT_REFRESH_TOKEN, requestBody);
    }

    public static Response getUserProfile(String token) {
        return getWithToken(ENDPOINT_GET_USER_PROFILE, token);
    }

    public static Response updateUserProfile(String newName, String newEmail, String token) {
        String requestBody = "{\"name\":\"" + newName + "\", \"email\":\"" + newEmail + "\"}";
        return patchWithJsonBody(ENDPOINT_UPDATE_USER_PROFILE, requestBody, token);
    }

    public static Response deleteUser(String token) {
        return deleteWithToken(ENDPOINT_DELETE_USER, token);
    }

    public static Response getAllOrders(String token) {
        return getWithToken(ENDPOINT_GET_ALL_ORDERS, token);
    }

    public static Response getUserOrders(String token) {
        return getWithToken(ENDPOINT_GET_USER_ORDERS, token);
    }

    // ... other helper methods ...

    private static Response get(String endpoint) {
        return given()
                .when()
                .get(endpoint);
    }

    private static Response post(String endpoint, String requestBody) {
        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(endpoint);
    }

    private static Response post(String endpoint, String requestBody, String token) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(requestBody)
                .when()
                .post(endpoint);
    }

    private static Response patchWithJsonBody(String endpoint, String requestBody, String token) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(requestBody)
                .when()
                .patch(endpoint);
    }

    private static Response deleteWithToken(String endpoint, String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete(endpoint);
    }

    private static Response getWithToken(String endpoint, String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(endpoint);
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
}
