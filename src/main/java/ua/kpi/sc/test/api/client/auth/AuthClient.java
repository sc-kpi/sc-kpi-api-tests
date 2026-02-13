package ua.kpi.sc.test.api.client.auth;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.model.auth.ForgotPasswordRequest;
import ua.kpi.sc.test.api.model.auth.LoginRequest;
import ua.kpi.sc.test.api.model.auth.RegisterRequest;
import ua.kpi.sc.test.api.model.auth.ResetPasswordRequest;
import ua.kpi.sc.test.api.util.JsonHelper;

public class AuthClient extends ApiClient {

    @Step("POST /auth/login — Login with email: {request.email}")
    public Response login(LoginRequest request) {
        return post(Endpoint.AUTH_LOGIN, request);
    }

    @Step("POST /auth/register — Register user: {request.email}")
    public Response register(RegisterRequest request) {
        return post(Endpoint.AUTH_REGISTER, request);
    }

    @Step("POST /auth/refresh — Refresh token via cookie")
    public Response refreshToken(String refreshTokenCookie) {
        return postWithCookie(Endpoint.AUTH_REFRESH, "refresh_token", refreshTokenCookie);
    }

    @Step("POST /auth/logout — Logout user")
    public Response logout(String accessToken) {
        return postEmpty(Endpoint.AUTH_LOGOUT, accessToken);
    }

    @Step("GET /auth/me — Get current user info")
    public Response getMe(String accessToken) {
        return get(Endpoint.AUTH_ME, accessToken);
    }

    @Step("POST /auth/logout — Logout without auth")
    public Response logoutNoAuth() {
        return postEmpty(Endpoint.AUTH_LOGOUT);
    }

    @Step("POST /auth/register — Register with raw body")
    public Response registerRaw(String rawBody) {
        return postRaw(Endpoint.AUTH_REGISTER, rawBody);
    }

    @Step("POST /auth/login — Login with raw body")
    public Response loginRaw(String rawBody) {
        return postRaw(Endpoint.AUTH_LOGIN, rawBody);
    }

    @Step("GET /auth/me — Get current user via cookie")
    public Response getMeWithCookie(String accessToken) {
        return getWithCookie(Endpoint.AUTH_ME, "access_token", accessToken);
    }

    @Step("POST /auth/register — Register with Content-Type: {contentType}")
    public Response registerWithContentType(RegisterRequest request, String contentType) {
        return postRawWithContentType(Endpoint.AUTH_REGISTER, JsonHelper.toJson(request), contentType);
    }

    @Step("POST /auth/login — Login with Content-Type: {contentType}")
    public Response loginWithContentType(LoginRequest request, String contentType) {
        return postRawWithContentType(Endpoint.AUTH_LOGIN, JsonHelper.toJson(request), contentType);
    }

    @Step("POST /auth/forgot-password — Request password reset for: {request.email}")
    public Response forgotPassword(ForgotPasswordRequest request) {
        return post(Endpoint.AUTH_FORGOT_PASSWORD, request);
    }

    @Step("POST /auth/forgot-password — Request with raw body")
    public Response forgotPasswordRaw(String rawBody) {
        return postRaw(Endpoint.AUTH_FORGOT_PASSWORD, rawBody);
    }

    @Step("POST /auth/reset-password — Reset password with token")
    public Response resetPassword(ResetPasswordRequest request) {
        return post(Endpoint.AUTH_RESET_PASSWORD, request);
    }

    @Step("POST /auth/reset-password — Reset with raw body")
    public Response resetPasswordRaw(String rawBody) {
        return postRaw(Endpoint.AUTH_RESET_PASSWORD, rawBody);
    }

    @Step("GET /auth/oauth2/google — Get Google OAuth redirect URL")
    public Response googleOAuthRedirect() {
        return getNoRedirect(Endpoint.AUTH_OAUTH2_GOOGLE);
    }

    @Step("GET /auth/oauth2/callback/google — Handle Google OAuth callback")
    public Response googleOAuthCallback(String code, String state, String stateCookie) {
        return getNoRedirectWithQueryParamsAndCookie(Endpoint.AUTH_OAUTH2_CALLBACK_GOOGLE,
                java.util.Map.of("code", code, "state", state),
                "oauth_state", stateCookie);
    }
}
