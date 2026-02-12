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
@Feature("Refresh Token")
public class AuthRefreshTest extends BaseAuthTest {

    // ==================== POST /refresh — POSITIVE (migrated) ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Refresh token rotates tokens and returns user data")
    public void refreshTokenReturnsNewToken() {
        Response regResponse = registerUniqueUser();
        String refreshToken = extractCookie(regResponse, "refresh_token");

        Response response = authClient.refreshToken(refreshToken);

        assertOk(response);
        assertThat(response.jsonPath().getString("email")).isNotNull();
        assertThat(extractCookie(response, "access_token")).isNotNull();
        String newRefreshToken = extractCookie(response, "refresh_token");
        assertThat(newRefreshToken).isNotNull();
        assertThat(newRefreshToken).isNotEqualTo(refreshToken);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Refresh rotates access token to a new value")
    public void refreshRotatesAccessToken() {
        Response regResponse = registerUniqueUser();
        String originalAccessToken = extractCookie(regResponse, "access_token");
        String refreshToken = extractCookie(regResponse, "refresh_token");

        Response response = authClient.refreshToken(refreshToken);

        assertOk(response);
        String newAccessToken = extractCookie(response, "access_token");
        assertThat(newAccessToken).isNotNull();
        assertThat(newAccessToken).isNotEqualTo(originalAccessToken);
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.SCHEMA},
            description = "Refresh returns same user data as registration")
    public void refreshReturnsSameUserData() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response regResponse = authClient.register(regRequest);
        assertCreated(regResponse);
        String refreshToken = extractCookie(regResponse, "refresh_token");

        Response response = authClient.refreshToken(refreshToken);

        assertOk(response);
        assertThat(response.jsonPath().getString("email")).isEqualTo(regRequest.getEmail());
        assertThat(response.jsonPath().getString("firstName")).isEqualTo(regRequest.getFirstName());
        assertThat(response.jsonPath().getString("lastName")).isEqualTo(regRequest.getLastName());
    }

    @Test(groups = {TestGroup.SCHEMA},
            description = "Refresh response matches auth-user-response schema")
    public void refreshResponseMatchesSchema() {
        Response regResponse = registerUniqueUser();
        String refreshToken = extractCookie(regResponse, "refresh_token");

        Response response = authClient.refreshToken(refreshToken);

        assertOk(response);
        SchemaValidator.validateSchema(response, "auth-user-response.json");
    }

    // ==================== POST /refresh — POSITIVE (new) ====================

    @Test(groups = {TestGroup.POSITIVE, TestGroup.SCHEMA},
            description = "Refresh returns a valid UUID as id")
    public void refreshReturnsValidUuid() {
        Response regResponse = registerUniqueUser();
        String refreshToken = extractCookie(regResponse, "refresh_token");

        Response response = authClient.refreshToken(refreshToken);

        assertOk(response);
        String id = response.jsonPath().getString("id");
        assertThat(id).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Refresh returns capabilityTier >= 0")
    public void refreshReturnsCapabilityTier() {
        Response regResponse = registerUniqueUser();
        String refreshToken = extractCookie(regResponse, "refresh_token");

        Response response = authClient.refreshToken(refreshToken);

        assertOk(response);
        assertThat(response.jsonPath().getInt("capabilityTier")).isGreaterThanOrEqualTo(0);
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.CONTRACT},
            description = "Refresh response content type is application/json")
    public void refreshResponseContentTypeIsJson() {
        Response regResponse = registerUniqueUser();
        String refreshToken = extractCookie(regResponse, "refresh_token");

        Response response = authClient.refreshToken(refreshToken);

        assertOk(response);
        assertThat(response.getContentType()).contains("application/json");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Refresh sets both access_token and refresh_token cookies")
    public void refreshSetsBothCookies() {
        Response regResponse = registerUniqueUser();
        String refreshToken = extractCookie(regResponse, "refresh_token");

        Response response = authClient.refreshToken(refreshToken);

        assertOk(response);
        assertThat(extractCookie(response, "access_token")).isNotNull();
        assertThat(extractCookie(response, "refresh_token")).isNotNull();
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.SECURITY},
            description = "Refresh sets HttpOnly cookies")
    public void refreshSetsHttpOnlyCookies() {
        Response regResponse = registerUniqueUser();
        String refreshToken = extractCookie(regResponse, "refresh_token");

        Response response = authClient.refreshToken(refreshToken);

        assertOk(response);
        Cookie accessToken = extractDetailedCookie(response, "access_token");
        Cookie refreshTokenCookie = extractDetailedCookie(response, "refresh_token");
        assertThat(accessToken).isNotNull();
        assertThat(refreshTokenCookie).isNotNull();
        assertThat(accessToken.isHttpOnly()).isTrue();
        assertThat(refreshTokenCookie.isHttpOnly()).isTrue();
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Double refresh with new token succeeds")
    public void doubleRefreshWithNewTokenSucceeds() {
        Response regResponse = registerUniqueUser();
        String refreshToken = extractCookie(regResponse, "refresh_token");

        Response firstRefresh = authClient.refreshToken(refreshToken);
        assertOk(firstRefresh);
        String newRefreshToken = extractCookie(firstRefresh, "refresh_token");

        Response secondRefresh = authClient.refreshToken(newRefreshToken);

        assertOk(secondRefresh);
        assertThat(secondRefresh.jsonPath().getString("email")).isNotNull();
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Refresh preserves user id across rotations")
    public void refreshPreservesUserIdAcrossRotations() {
        Response regResponse = registerUniqueUser();
        String registeredId = regResponse.jsonPath().getString("id");
        String refreshToken = extractCookie(regResponse, "refresh_token");

        Response response = authClient.refreshToken(refreshToken);

        assertOk(response);
        assertThat(response.jsonPath().getString("id")).isEqualTo(registeredId);
    }

    // ==================== POST /refresh — NEGATIVE (migrated) ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Refresh with invalid token returns 401")
    public void refreshInvalidTokenReturns401() {
        Response response = authClient.refreshToken("invalid-token-value");

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Refresh with empty cookie returns 401")
    public void refreshWithEmptyCookieReturns401() {
        Response response = authClient.refreshToken("");

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Refresh with no cookie returns 401")
    public void refreshWithNoCookieReturns401() {
        Response response = authClient.postEmpty(Endpoint.AUTH_REFRESH);

        assertStatus(response, 401);
    }

    // ==================== POST /refresh — NEGATIVE (new) ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Refresh with malformed JWT token returns 401")
    public void refreshWithMalformedTokenReturns401() {
        Response response = authClient.refreshToken("not.a.valid.jwt.token");

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.SECURITY},
            description = "Refresh with access token used as refresh cookie returns 401")
    public void refreshWithAccessTokenAsCookieReturns401() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.refreshToken(accessToken);

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Refresh with expired/garbage JWT returns 401")
    public void refreshWithExpiredRefreshTokenReturns401() {
        Response response = authClient.refreshToken(
                "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiZXhwIjoxfQ.abc");

        assertStatus(response, 401);
    }

    // ==================== POST /refresh — CONTRACT (new) ====================

    @Test(groups = {TestGroup.CONTRACT},
            description = "Refresh 401 matches ProblemDetail schema")
    public void refreshProblemDetailSchemaOn401() {
        Response response = authClient.refreshToken("invalid");

        assertStatus(response, 401);
        SchemaValidator.validateProblemDetailSchema(response);
    }

    @Test(groups = {TestGroup.CONTRACT, TestGroup.METHOD_NOT_ALLOWED},
            description = "GET /refresh 405 matches ProblemDetail schema")
    public void refresh405MatchesProblemDetailSchema() {
        Response response = authClient.get(Endpoint.AUTH_REFRESH);

        assertStatus(response, 405);
        SchemaValidator.validateProblemDetailSchema(response);
    }

    // ==================== POST /refresh — SECURITY (migrated) ====================

    @Test(groups = {TestGroup.SECURITY},
            description = "Refresh token cannot be reused after rotation")
    public void refreshTokenCannotBeReusedAfterRotation() {
        Response regResponse = registerUniqueUser();
        String refreshToken = extractCookie(regResponse, "refresh_token");

        // Use it once — should succeed
        Response firstRefresh = authClient.refreshToken(refreshToken);
        assertOk(firstRefresh);

        // Reuse the same token — should fail
        Response secondRefresh = authClient.refreshToken(refreshToken);
        assertStatus(secondRefresh, 401);
    }

    // ==================== POST /refresh — SECURITY (new) ====================

    @Test(groups = {TestGroup.SECURITY},
            description = "Refresh tokens are user-specific and return correct user data")
    public void refreshFromDifferentUserTokenFails() {
        // Register user A and get refresh token
        Response regResponseA = registerUniqueUser();
        String refreshTokenA = extractCookie(regResponseA, "refresh_token");

        // Register user B and get refresh token
        Response regResponseB = registerUniqueUser();
        String refreshTokenB = extractCookie(regResponseB, "refresh_token");

        // Refresh with token A — returns user A data
        Response refreshA = authClient.refreshToken(refreshTokenA);
        assertOk(refreshA);

        // Refresh with token B — returns user B data
        Response refreshB = authClient.refreshToken(refreshTokenB);
        assertOk(refreshB);

        // Verify the IDs differ — tokens are user-specific
        String idA = refreshA.jsonPath().getString("id");
        String idB = refreshB.jsonPath().getString("id");
        assertThat(idA).isNotEqualTo(idB);
    }

    // ==================== POST /refresh — METHOD_NOT_ALLOWED (migrated) ====================

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "GET /refresh returns 405")
    public void refreshMethodNotAllowedGet() {
        Response response = authClient.get(Endpoint.AUTH_REFRESH);

        assertStatus(response, 405);
    }

    // ==================== POST /refresh — METHOD_NOT_ALLOWED (new) ====================

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PUT /refresh returns 405")
    public void refreshMethodNotAllowedPut() {
        Response response = authClient.put(Endpoint.AUTH_REFRESH, "{}");

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "DELETE /refresh returns 405")
    public void refreshMethodNotAllowedDelete() {
        Response response = authClient.delete(Endpoint.AUTH_REFRESH);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PATCH /refresh returns 405")
    public void refreshMethodNotAllowedPatch() {
        Response response = authClient.patch(Endpoint.AUTH_REFRESH, "{}");

        assertStatus(response, 405);
    }
}
