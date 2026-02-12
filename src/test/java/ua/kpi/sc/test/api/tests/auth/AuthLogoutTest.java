package ua.kpi.sc.test.api.tests.auth;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.auth.RegisterRequest;
import ua.kpi.sc.test.api.util.SchemaValidator;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Authentication")
@Feature("Logout")
public class AuthLogoutTest extends BaseAuthTest {

    // ==================== POSITIVE ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Logout clears session and returns 200")
    public void logoutClearsSession() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.logout(accessToken);

        assertOk(response);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Logout clears token cookies with Max-Age=0")
    public void logoutClearsTokenCookies() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.logout(accessToken);

        assertOk(response);
        String setCookieHeader = response.getHeaders().getValues("Set-Cookie").toString();
        assertThat(setCookieHeader).containsIgnoringCase("access_token");
        assertThat(setCookieHeader).containsIgnoringCase("refresh_token");
        assertThat(setCookieHeader).containsIgnoringCase("Max-Age=0");
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.SECURITY},
            description = "Logout invalidates refresh token")
    public void logoutInvalidatesRefreshToken() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");
        String refreshToken = extractCookie(regResponse, "refresh_token");

        authClient.logout(accessToken);

        // Try to use old refresh token — should fail
        Response response = authClient.refreshToken(refreshToken);
        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Logout returns empty or no body")
    public void logoutReturnsEmptyOrNoBody() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.logout(accessToken);

        assertOk(response);
        assertThat(response.getBody().asString().length()).isLessThanOrEqualTo(4);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Logout clears access_token cookie specifically with Max-Age=0")
    public void logoutClearsAccessTokenCookieSpecifically() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.logout(accessToken);

        assertOk(response);
        String setCookies = response.getHeaders().getValues("Set-Cookie").toString();
        assertThat(setCookies).containsIgnoringCase("access_token");
        Cookie accessCookie = extractDetailedCookie(response, "access_token");
        assertThat(accessCookie).isNotNull();
        assertThat(accessCookie.getMaxAge()).isEqualTo(0);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Logout clears refresh_token cookie specifically with Max-Age=0")
    public void logoutClearsRefreshTokenCookieSpecifically() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.logout(accessToken);

        assertOk(response);
        Cookie refreshCookie = extractDetailedCookie(response, "refresh_token");
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.getMaxAge()).isEqualTo(0);
    }

    // ==================== NEGATIVE ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Logout without auth returns 401")
    public void logoutWithoutAuthReturns401() {
        Response response = authClient.logoutNoAuth();

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Logout with invalid bearer token returns 401")
    public void logoutWithInvalidTokenReturns401() {
        Response response = authClient.logout("invalid.bearer.token");

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Logout with expired JWT returns 401")
    public void logoutWithExpiredTokenReturns401() {
        Response response = authClient.logout(
                "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZXhwIjoxfQ.invalid");

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Logout is idempotent or rejects already-invalidated token")
    public void logoutIdempotent() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response firstLogout = authClient.logout(accessToken);
        assertOk(firstLogout);

        // Second logout with same token — may succeed (stateless JWT) or fail (server-side invalidation)
        Response secondLogout = authClient.logout(accessToken);
        assertThat(secondLogout.getStatusCode()).isIn(200, 401);
    }

    // ==================== CONTRACT ====================

    @Test(groups = {TestGroup.CONTRACT},
            description = "Logout 401 matches ProblemDetail schema")
    public void logoutProblemDetailSchemaOn401() {
        Response response = authClient.logoutNoAuth();

        assertStatus(response, 401);
        SchemaValidator.validateProblemDetailSchema(response);
    }

    @Test(groups = {TestGroup.CONTRACT, TestGroup.METHOD_NOT_ALLOWED},
            description = "GET /logout 405 matches ProblemDetail schema")
    public void logout405MatchesProblemDetailSchema() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.get(Endpoint.AUTH_LOGOUT, accessToken);

        assertStatus(response, 405);
        SchemaValidator.validateProblemDetailSchema(response);
    }

    // ==================== SECURITY ====================

    @Test(groups = {TestGroup.SECURITY},
            description = "Access token may still be valid for /me after logout (stateless JWT)")
    public void logoutAccessTokenStillValidForMe() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");
        String refreshToken = extractCookie(regResponse, "refresh_token");

        authClient.logout(accessToken);

        // JWT may be stateless (200) or server may check revocation (401)
        Response meResponse = authClient.getMe(accessToken);
        assertThat(meResponse.getStatusCode()).isIn(200, 401);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Logout with cookie-based auth may or may not be accepted")
    public void logoutWithCookieAuthWorks() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        // Send logout using cookie auth instead of Bearer header
        Response response = authClient.postWithCookie(Endpoint.AUTH_LOGOUT, "access_token", accessToken);

        // Cookie auth may work for logout (200) or only Bearer may be accepted (401)
        assertThat(response.getStatusCode()).isIn(200, 401);
    }

    // ==================== METHOD_NOT_ALLOWED ====================

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "GET /logout returns 405")
    public void logoutMethodNotAllowedGet() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.get(Endpoint.AUTH_LOGOUT, accessToken);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PUT /logout returns 405")
    public void logoutMethodNotAllowedPut() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.put(Endpoint.AUTH_LOGOUT, "{}", accessToken);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "DELETE /logout returns 405")
    public void logoutMethodNotAllowedDelete() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.delete(Endpoint.AUTH_LOGOUT, accessToken);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PATCH /logout returns 405")
    public void logoutMethodNotAllowedPatch() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.patch(Endpoint.AUTH_LOGOUT, "{}", accessToken);

        assertStatus(response, 405);
    }
}
