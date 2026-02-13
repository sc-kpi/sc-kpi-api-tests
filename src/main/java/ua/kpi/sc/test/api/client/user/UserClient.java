package ua.kpi.sc.test.api.client.user;

import java.util.Map;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.model.user.AssignPartnerLevelRequest;
import ua.kpi.sc.test.api.model.user.ChangePasswordRequest;
import ua.kpi.sc.test.api.model.user.CreateUserRequest;
import ua.kpi.sc.test.api.model.user.UpdateStatusRequest;
import ua.kpi.sc.test.api.model.user.UpdateTierRequest;
import ua.kpi.sc.test.api.model.user.UserUpdateRequest;

public class UserClient extends ApiClient {

    @Step("GET /users — List all users")
    public Response getUsers(String authToken) {
        return get(Endpoint.USERS, authToken);
    }

    @Step("GET /users with query params")
    public Response getUsers(String authToken, Map<String, String> queryParams) {
        RequestSpecification spec = requestSpec(authToken);
        queryParams.forEach(spec::queryParam);
        return spec.get(Endpoint.USERS);
    }

    @Step("GET /users/{id} — Get user by ID: {id}")
    public Response getUserById(String id, String authToken) {
        return get(Endpoint.USERS + "/" + id, authToken);
    }

    @Step("GET /users/me — Get current user profile")
    public Response getCurrentUser(String authToken) {
        return get(Endpoint.USER_ME, authToken);
    }

    @Step("POST /users — Create user")
    public Response createUser(CreateUserRequest request, String authToken) {
        return post(Endpoint.USERS, request, authToken);
    }

    @Step("PATCH /users/{id} — Update user: {id}")
    public Response updateUser(String id, UserUpdateRequest request, String authToken) {
        return patch(Endpoint.USERS + "/" + id, request, authToken);
    }

    @Step("PUT /users/{id} — Update user (PUT): {id}")
    public Response updateUserPut(String id, UserUpdateRequest request, String authToken) {
        return put(Endpoint.USERS + "/" + id, request, authToken);
    }

    @Step("PATCH /users/me — Update self profile")
    public Response updateSelf(UserUpdateRequest request, String authToken) {
        return patch(Endpoint.USER_ME, request, authToken);
    }

    @Step("PUT /users/me — Update self profile (PUT)")
    public Response updateSelfPut(UserUpdateRequest request, String authToken) {
        return put(Endpoint.USER_ME, request, authToken);
    }

    @Step("PATCH /users/{id}/tier — Update user tier: {id}")
    public Response updateTier(String id, UpdateTierRequest request, String authToken) {
        return patch(Endpoint.USERS + "/" + id + "/tier", request, authToken);
    }

    @Step("PUT /users/{id}/tier — Update user tier (PUT): {id}")
    public Response updateTierPut(String id, UpdateTierRequest request, String authToken) {
        return put(Endpoint.USERS + "/" + id + "/tier", request, authToken);
    }

    @Step("PATCH /users/{id}/status — Update user status: {id}")
    public Response updateStatus(String id, UpdateStatusRequest request, String authToken) {
        return patch(Endpoint.USERS + "/" + id + "/status", request, authToken);
    }

    @Step("PUT /users/{id}/status — Update user status (PUT): {id}")
    public Response updateStatusPut(String id, UpdateStatusRequest request, String authToken) {
        return put(Endpoint.USERS + "/" + id + "/status", request, authToken);
    }

    @Step("DELETE /users/{id} — Delete user: {id}")
    public Response deleteUser(String id, String authToken) {
        return delete(Endpoint.USERS + "/" + id, authToken);
    }

    @Step("POST /users/{userId}/partners — Assign partner level")
    public Response assignPartnerLevel(String userId, AssignPartnerLevelRequest request, String authToken) {
        return post(Endpoint.USERS + "/" + userId + "/partners", request, authToken);
    }

    @Step("DELETE /users/{userId}/partners/{partnerId} — Remove partner level")
    public Response removePartnerLevel(String userId, String partnerId, String authToken) {
        return delete(Endpoint.USERS + "/" + userId + "/partners/" + partnerId, authToken);
    }

    @Step("PATCH /users/me/password — Change password")
    public Response changePassword(ChangePasswordRequest request, String authToken) {
        return patch(Endpoint.USER_ME_PASSWORD, request, authToken);
    }
}
