package ua.kpi.sc.test.api.client.user;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.model.user.UserUpdateRequest;

public class UserClient extends ApiClient {

    @Step("GET /users — List all users")
    public Response getUsers(String authToken) {
        return get(Endpoint.USERS, authToken);
    }

    @Step("GET /users/{id} — Get user by ID: {id}")
    public Response getUserById(String id, String authToken) {
        return get(Endpoint.USERS + "/" + id, authToken);
    }

    @Step("GET /users/me — Get current user profile")
    public Response getCurrentUser(String authToken) {
        return get(Endpoint.USER_ME, authToken);
    }

    @Step("PUT /users/{id} — Update user: {id}")
    public Response updateUser(String id, UserUpdateRequest request, String authToken) {
        return put(Endpoint.USERS + "/" + id, request, authToken);
    }

    @Step("DELETE /users/{id} — Delete user: {id}")
    public Response deleteUser(String id, String authToken) {
        return delete(Endpoint.USERS + "/" + id, authToken);
    }
}
