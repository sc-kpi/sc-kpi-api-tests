package ua.kpi.sc.test.api.tests.auth;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.auth.LoginRequest;
import ua.kpi.sc.test.api.model.auth.RegisterRequest;
import ua.kpi.sc.test.api.util.SchemaValidator;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Authentication")
@Feature("Get Current User")
public class AuthMeTest extends BaseAuthTest {

    // ==================== GET /me — POSITIVE (migrated) ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "GET /auth/me returns current user profile with all key fields")
    public void meReturnsProfile() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.getMe(accessToken);

        assertOk(response);
        assertThat(response.jsonPath().getString("id")).isNotNull();
        assertThat(response.jsonPath().getString("email")).isNotNull();
        assertThat(response.jsonPath().getString("firstName")).isNotNull();
        assertThat(response.jsonPath().getString("lastName")).isNotNull();
        assertThat(response.jsonPath().getInt("capabilityTier")).isGreaterThanOrEqualTo(0);
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.SCHEMA},
            description = "GET /me returns all required fields non-null")
    public void meReturnsAllRequiredFields() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.getMe(accessToken);

        assertOk(response);
        assertThat(response.jsonPath().getString("id")).isNotNull();
        assertThat(response.jsonPath().getString("email")).isNotNull();
        assertThat(response.jsonPath().getString("firstName")).isNotNull();
        assertThat(response.jsonPath().getString("lastName")).isNotNull();
        assertThat((Object) response.jsonPath().get("capabilityTier")).isNotNull();
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "GET /me returns correct data matching registration request")
    public void meReturnsCorrectRegisteredData() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response regResponse = authClient.register(regRequest);
        assertCreated(regResponse);
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.getMe(accessToken);

        assertOk(response);
        assertThat(response.jsonPath().getString("email")).isEqualTo(regRequest.getEmail());
        assertThat(response.jsonPath().getString("firstName")).isEqualTo(regRequest.getFirstName());
        assertThat(response.jsonPath().getString("lastName")).isEqualTo(regRequest.getLastName());
    }

    @Test(groups = {TestGroup.SCHEMA},
            description = "GET /me response matches auth-user-response schema")
    public void meResponseMatchesSchema() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.getMe(accessToken);

        assertOk(response);
        SchemaValidator.validateSchema(response, "auth-user-response.json");
    }

    // ==================== GET /me — POSITIVE (new) ====================

    @Test(groups = {TestGroup.POSITIVE, TestGroup.SCHEMA},
            description = "GET /me returns a valid UUID as id")
    public void meReturnsValidUuid() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.getMe(accessToken);

        assertOk(response);
        String id = response.jsonPath().getString("id");
        assertThat(id).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "GET /me returns capabilityTier 1 for newly registered user")
    public void meReturnsCapabilityTier1ForNewUser() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.getMe(accessToken);

        assertOk(response);
        assertThat(response.jsonPath().getInt("capabilityTier")).isEqualTo(1);
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.CONTRACT},
            description = "GET /me response Content-Type is application/json")
    public void meResponseContentTypeIsJson() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.getMe(accessToken);

        assertOk(response);
        assertThat(response.getContentType()).contains("application/json");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "GET /me with access_token cookie succeeds")
    public void meWithAccessTokenCookieSucceeds() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.getMeWithCookie(accessToken);

        assertOk(response);
        assertThat(response.jsonPath().getString("email")).isNotNull();
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "GET /me returns same data as registration response")
    public void meReturnsSameDataAsRegistration() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response regResponse = authClient.register(regRequest);
        assertCreated(regResponse);
        String accessToken = extractCookie(regResponse, "access_token");

        String regEmail = regResponse.jsonPath().getString("email");
        String regFirstName = regResponse.jsonPath().getString("firstName");
        String regLastName = regResponse.jsonPath().getString("lastName");

        Response response = authClient.getMe(accessToken);

        assertOk(response);
        assertThat(response.jsonPath().getString("email")).isEqualTo(regEmail);
        assertThat(response.jsonPath().getString("firstName")).isEqualTo(regFirstName);
        assertThat(response.jsonPath().getString("lastName")).isEqualTo(regLastName);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "GET /me returns same data as login response")
    public void meReturnsSameDataAsLogin() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response loginResponse = registerAndLogin(regRequest);
        assertOk(loginResponse);

        String loginEmail = loginResponse.jsonPath().getString("email");
        String loginFirstName = loginResponse.jsonPath().getString("firstName");
        String loginLastName = loginResponse.jsonPath().getString("lastName");
        String loginId = loginResponse.jsonPath().getString("id");
        String accessToken = extractCookie(loginResponse, "access_token");

        Response response = authClient.getMe(accessToken);

        assertOk(response);
        assertThat(response.jsonPath().getString("email")).isEqualTo(loginEmail);
        assertThat(response.jsonPath().getString("firstName")).isEqualTo(loginFirstName);
        assertThat(response.jsonPath().getString("lastName")).isEqualTo(loginLastName);
        assertThat(response.jsonPath().getString("id")).isEqualTo(loginId);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "GET /me is idempotent — two calls return identical data")
    public void meIdempotent() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response first = authClient.getMe(accessToken);
        Response second = authClient.getMe(accessToken);

        assertOk(first);
        assertOk(second);
        assertThat(first.jsonPath().getString("id")).isEqualTo(second.jsonPath().getString("id"));
        assertThat(first.jsonPath().getString("email")).isEqualTo(second.jsonPath().getString("email"));
        assertThat(first.jsonPath().getString("firstName")).isEqualTo(second.jsonPath().getString("firstName"));
        assertThat(first.jsonPath().getString("lastName")).isEqualTo(second.jsonPath().getString("lastName"));
    }

    // ==================== GET /me — NEGATIVE (migrated) ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "GET /auth/me without token returns 401")
    public void meWithoutAuthReturns401() {
        Response response = authClient.get(Endpoint.AUTH_ME);

        assertStatus(response, 401);
    }

    // ==================== GET /me — NEGATIVE (new) ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "GET /me with malformed Bearer token returns 401")
    public void meWithMalformedBearerTokenReturns401() {
        Response response = authClient.getMe("not.a.jwt");

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "GET /me with empty Bearer token returns 401")
    public void meWithEmptyBearerTokenReturns401() {
        Response response = authClient.getMe("");

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.SECURITY},
            description = "GET /me with tampered JWT signature returns 401")
    public void meWithTamperedJwtSignatureReturns401() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        String tampered = accessToken.substring(0, accessToken.length() - 3) + "xxx";

        Response response = authClient.getMe(tampered);

        assertStatus(response, 401);
    }

    // ==================== GET /me — SECURITY (migrated) ====================

    @Test(groups = {TestGroup.SECURITY},
            description = "Expired/invalid access token returns 401")
    public void expiredTokenReturns401() {
        Response response = authClient.getMe("expired.invalid.token");

        assertStatus(response, 401);
    }

    // ==================== GET /me — CONTRACT (migrated) ====================

    @Test(groups = {TestGroup.CONTRACT},
            description = "GET /me 401 matches ProblemDetail schema")
    public void meProblemDetailSchemaOn401() {
        Response response = authClient.get(Endpoint.AUTH_ME);

        assertStatus(response, 401);
        SchemaValidator.validateProblemDetailSchema(response);
    }

    // ==================== GET /me — CONTRACT (new) ====================

    @Test(groups = {TestGroup.CONTRACT, TestGroup.METHOD_NOT_ALLOWED},
            description = "POST /me 405 matches ProblemDetail schema")
    public void me405MatchesProblemDetailSchema() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.post(Endpoint.AUTH_ME, "{}", accessToken);

        assertStatus(response, 405);
        SchemaValidator.validateProblemDetailSchema(response);
    }

    // ==================== GET /me — METHOD_NOT_ALLOWED (migrated) ====================

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "POST /me returns 405")
    public void meMethodNotAllowedPost() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.post(Endpoint.AUTH_ME, "{}", accessToken);

        assertStatus(response, 405);
    }

    // ==================== GET /me — METHOD_NOT_ALLOWED (new) ====================

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PUT /me returns 405")
    public void meMethodNotAllowedPut() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.put(Endpoint.AUTH_ME, "{}", accessToken);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "DELETE /me returns 405")
    public void meMethodNotAllowedDelete() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.delete(Endpoint.AUTH_ME, accessToken);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PATCH /me returns 405")
    public void meMethodNotAllowedPatch() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.patch(Endpoint.AUTH_ME, "{}", accessToken);

        assertStatus(response, 405);
    }

    // ==================== GET /me — SECURITY (new) ====================

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.SECURITY},
            description = "GET /me with refresh token instead of access token returns 401")
    public void meWithRefreshTokenReturns401() {
        String refreshToken = registerAndGetRefreshToken();

        Response response = authClient.getMe(refreshToken);

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "GET /me response does not contain password field")
    public void meDoesNotReturnPasswordField() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.getMe(accessToken);

        assertOk(response);
        assertThat(response.getBody().asString()).doesNotContain("\"password\"");
    }
}
