package ua.kpi.sc.test.api.client.auth;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.model.auth.LoginRequest;
import ua.kpi.sc.test.api.model.auth.RegisterRequest;
import ua.kpi.sc.test.api.model.auth.TokenRefreshRequest;

public class AuthClient extends ApiClient {

    @Step("POST /auth/login — Login with email: {request.email}")
    public Response login(LoginRequest request) {
        return post(Endpoint.AUTH_LOGIN, request);
    }

    @Step("POST /auth/register — Register user: {request.email}")
    public Response register(RegisterRequest request) {
        return post(Endpoint.AUTH_REGISTER, request);
    }

    @Step("POST /auth/refresh — Refresh token")
    public Response refreshToken(TokenRefreshRequest request) {
        return post(Endpoint.AUTH_REFRESH, request);
    }
}
