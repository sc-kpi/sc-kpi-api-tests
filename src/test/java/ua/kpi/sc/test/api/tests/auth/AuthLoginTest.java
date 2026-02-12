package ua.kpi.sc.test.api.tests.auth;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.http.Cookie;
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
@Feature("Login")
public class AuthLoginTest extends BaseAuthTest {

    // ==================== SMOKE ====================

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "Login with valid credentials returns 200 with email and cookies")
    public void loginWithValidCredentials() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();

        Response response = authClient.login(loginRequest);

        assertOk(response);
        assertThat(response.jsonPath().getString("email")).isEqualTo(regRequest.getEmail());
        assertThat(extractCookie(response, "access_token")).isNotNull();
        assertThat(extractCookie(response, "refresh_token")).isNotNull();
    }

    // ==================== POST /login — POSITIVE ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Login returns the same email as registered")
    public void loginReturnsSameEmailAsRegistered() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();

        Response response = authClient.login(loginRequest);

        assertOk(response);
        assertThat(response.jsonPath().getString("email")).isEqualTo(regRequest.getEmail());
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.SCHEMA},
            description = "Login returns both access and refresh cookies")
    public void loginReturnsAccessAndRefreshCookies() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();

        Response response = authClient.login(loginRequest);

        assertOk(response);
        assertThat(extractCookie(response, "access_token")).isNotNull();
        assertThat(extractCookie(response, "refresh_token")).isNotNull();
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.SECURITY},
            description = "Login sets HttpOnly cookies")
    public void loginSetsHttpOnlyCookies() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();

        Response response = authClient.login(loginRequest);

        assertOk(response);
        Cookie accessToken = extractDetailedCookie(response, "access_token");
        Cookie refreshToken = extractDetailedCookie(response, "refresh_token");
        assertThat(accessToken).isNotNull();
        assertThat(refreshToken).isNotNull();
        assertThat(accessToken.isHttpOnly()).isTrue();
        assertThat(refreshToken.isHttpOnly()).isTrue();
    }

    @Test(groups = {TestGroup.SCHEMA},
            description = "Login response matches auth-user-response schema")
    public void loginResponseMatchesSchema() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();

        Response response = authClient.login(loginRequest);

        assertOk(response);
        SchemaValidator.validateSchema(response, "auth-user-response.json");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Login returns same firstName and lastName as registered")
    public void loginReturnsSameFirstAndLastName() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();

        Response response = authClient.login(loginRequest);

        assertOk(response);
        assertThat(response.jsonPath().getString("firstName")).isEqualTo(regRequest.getFirstName());
        assertThat(response.jsonPath().getString("lastName")).isEqualTo(regRequest.getLastName());
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Login returns capabilityTier that is not null and >= 0")
    public void loginReturnsCapabilityTier() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();

        Response response = authClient.login(loginRequest);

        assertOk(response);
        assertThat((Object) response.jsonPath().get("capabilityTier")).isNotNull();
        assertThat(response.jsonPath().getInt("capabilityTier")).isGreaterThanOrEqualTo(0);
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.SCHEMA},
            description = "Login returns a valid UUID as id")
    public void loginReturnsValidUuid() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();

        Response response = authClient.login(loginRequest);

        assertOk(response);
        String id = response.jsonPath().getString("id");
        assertThat(id).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.CONTRACT},
            description = "Login response Content-Type is application/json")
    public void loginResponseContentTypeIsJson() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();

        Response response = authClient.login(loginRequest);

        assertOk(response);
        assertThat(response.getContentType()).contains("application/json");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Login multiple times with same credentials succeeds")
    public void loginMultipleTimesSucceeds() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();

        Response response1 = authClient.login(loginRequest);
        Response response2 = authClient.login(loginRequest);

        assertOk(response1);
        assertOk(response2);
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.SECURITY},
            description = "Login returns different access tokens each time")
    public void loginReturnsDifferentTokensEachTime() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();

        Response response1 = authClient.login(loginRequest);
        Response response2 = authClient.login(loginRequest);

        assertOk(response1);
        assertOk(response2);
        String token1 = extractCookie(response1, "access_token");
        String token2 = extractCookie(response2, "access_token");
        assertThat(token1).isNotEqualTo(token2);
    }

    // ==================== POST /login — NEGATIVE ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Login with wrong password returns 401")
    public void loginWithInvalidCredentialsReturns401() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password("wrong-password-123")
                .build();

        Response response = authClient.login(loginRequest);

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Login with non-existent email returns 401")
    public void loginNonExistentEmailReturns401() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("nonexistent-" + System.nanoTime() + "@kpi.ua")
                .password("password123")
                .build();

        Response response = authClient.login(loginRequest);

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Login with invalid email format returns 400")
    public void loginWithInvalidEmailFormatReturns400() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("bad")
                .password("password123")
                .build();

        Response response = authClient.login(loginRequest);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Login with empty fields returns 400")
    public void loginEmptyFieldsReturns400() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("")
                .password("")
                .build();

        Response response = authClient.login(loginRequest);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Login with empty body returns 400")
    public void loginWithEmptyBodyReturns400() {
        Response response = authClient.postEmpty(Endpoint.AUTH_LOGIN);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Login with malformed JSON returns 400")
    public void loginWithMalformedJsonReturns400() {
        Response response = authClient.loginRaw("{invalid");

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Login with null email returns 400")
    public void loginWithNullEmailReturns400() {
        LoginRequest loginRequest = LoginRequest.builder()
                .password("password123")
                .build();

        Response response = authClient.login(loginRequest);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Login with null password returns 400")
    public void loginWithNullPasswordReturns400() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email(TestDataFactory.randomEmail())
                .build();

        Response response = authClient.login(loginRequest);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Login with whitespace-only email returns 400")
    public void loginWithWhitespaceEmailReturns400() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("   ")
                .password("password123")
                .build();

        Response response = authClient.login(loginRequest);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Login with empty JSON object returns 400")
    public void loginWithEmptyJsonObjectReturns400() {
        Response response = authClient.loginRaw("{}");

        assertStatus(response, 400);
    }

    // ==================== POST /login — CONTRACT ====================

    @Test(groups = {TestGroup.CONTRACT},
            description = "Login 401 matches ProblemDetail schema")
    public void loginProblemDetailSchemaOn401() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("nonexistent-" + System.nanoTime() + "@kpi.ua")
                .password("password123")
                .build();

        Response response = authClient.login(loginRequest);

        assertStatus(response, 401);
        SchemaValidator.validateProblemDetailSchema(response);
    }

    @Test(groups = {TestGroup.CONTRACT},
            description = "Login 400 matches ProblemDetail schema")
    public void loginProblemDetailSchemaOn400() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("bad")
                .password("password123")
                .build();

        Response response = authClient.login(loginRequest);

        assertStatus(response, 400);
        SchemaValidator.validateProblemDetailSchema(response);
    }

    @Test(groups = {TestGroup.CONTRACT, TestGroup.SECURITY},
            description = "Login 401 does not reveal whether user exists")
    public void login401DoesNotRevealUserExistence() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        // Wrong password for existing user
        LoginRequest wrongPw = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password("wrong-password-123")
                .build();
        Response r1 = authClient.login(wrongPw);
        assertStatus(r1, 401);

        // Non-existent user
        LoginRequest nonExistent = LoginRequest.builder()
                .email("nonexistent-" + System.nanoTime() + "@kpi.ua")
                .password("password123")
                .build();
        Response r2 = authClient.login(nonExistent);
        assertStatus(r2, 401);

        // Same error message
        assertThat(r1.jsonPath().getString("detail"))
                .isEqualTo(r2.jsonPath().getString("detail"));
    }

    @Test(groups = {TestGroup.CONTRACT, TestGroup.METHOD_NOT_ALLOWED},
            description = "GET /login returns 405 with ProblemDetail schema")
    public void login405MatchesProblemDetailSchema() {
        Response response = authClient.get(Endpoint.AUTH_LOGIN);

        assertStatus(response, 405);
        SchemaValidator.validateProblemDetailSchema(response);
    }

    // ==================== POST /login — CONTENT-TYPE ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Login with XML Content-Type returns 415")
    public void loginWithXmlContentTypeReturns415() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();

        Response response = authClient.loginWithContentType(loginRequest, "application/xml");

        assertStatus(response, 415);
    }

    // ==================== POST /login — METHOD_NOT_ALLOWED ====================

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "GET /login returns 405")
    public void loginMethodNotAllowedGet() {
        Response response = authClient.get(Endpoint.AUTH_LOGIN);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PUT /login returns 405")
    public void loginMethodNotAllowedPut() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("password123")
                .build();

        Response response = authClient.put(Endpoint.AUTH_LOGIN, loginRequest);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "DELETE /login returns 405")
    public void loginMethodNotAllowedDelete() {
        Response response = authClient.delete(Endpoint.AUTH_LOGIN);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PATCH /login returns 405")
    public void loginMethodNotAllowedPatch() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("password123")
                .build();

        Response response = authClient.patch(Endpoint.AUTH_LOGIN, loginRequest);

        assertStatus(response, 405);
    }

    // ==================== POST /login — SECURITY ====================

    @Test(groups = {TestGroup.SECURITY},
            description = "Login with SQL injection in email is rejected with 400 or 401")
    public void loginWithSqlInjectionInEmailReturns401Or400() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("' OR 1=1 --@test.com")
                .password("password123")
                .build();

        Response response = authClient.login(loginRequest);

        assertThat(response.getStatusCode()).isIn(400, 401);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Login with XSS in email returns 400")
    public void loginWithXssInEmailReturns400() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("<script>alert(1)</script>@test.com")
                .password("password123")
                .build();

        Response response = authClient.login(loginRequest);

        assertStatus(response, 400);
    }
}
