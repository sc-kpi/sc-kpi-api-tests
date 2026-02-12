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
@Feature("Register")
public class AuthRegisterTest extends BaseAuthTest {

    // ==================== SMOKE ====================

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "Register new user returns 201 with cookies and user data")
    public void registerNewUser() {
        RegisterRequest request = TestDataFactory.validRegisterRequest();

        Response response = authClient.register(request);

        assertCreated(response);
        assertThat(response.jsonPath().getString("email")).isEqualTo(request.getEmail());
        assertThat(response.jsonPath().getString("firstName")).isEqualTo(request.getFirstName());
        assertThat(response.jsonPath().getString("lastName")).isEqualTo(request.getLastName());
        assertThat(response.jsonPath().getString("id")).isNotNull();
        assertThat(extractCookie(response, "access_token")).isNotNull();
        assertThat(extractCookie(response, "refresh_token")).isNotNull();
    }

    // ==================== POSITIVE ====================

    @Test(groups = {TestGroup.POSITIVE, TestGroup.SCHEMA},
            description = "Register returns a valid UUID as id")
    public void registerReturnsValidUuid() {
        Response response = registerUniqueUser();

        String id = response.jsonPath().getString("id");
        assertThat(id).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.SCHEMA},
            description = "Register returns capabilityTier >= 0")
    public void registerReturnsCapabilityTier() {
        Response response = registerUniqueUser();

        assertThat(response.jsonPath().getInt("capabilityTier")).isGreaterThanOrEqualTo(0);
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.SECURITY},
            description = "Register sets HttpOnly cookies")
    public void registerSetsHttpOnlyCookies() {
        Response response = registerUniqueUser();

        Cookie accessToken = extractDetailedCookie(response, "access_token");
        Cookie refreshToken = extractDetailedCookie(response, "refresh_token");
        assertThat(accessToken).isNotNull();
        assertThat(refreshToken).isNotNull();
        assertThat(accessToken.isHttpOnly()).isTrue();
        assertThat(refreshToken.isHttpOnly()).isTrue();
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Register with Unicode (Cyrillic) names succeeds")
    public void registerWithUnicodeNamesSucceeds() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("password123")
                .firstName("Дмитро")
                .lastName("Шліханов")
                .build();

        Response response = authClient.register(request);

        assertCreated(response);
        assertThat(response.jsonPath().getString("firstName")).isEqualTo("Дмитро");
        assertThat(response.jsonPath().getString("lastName")).isEqualTo("Шліханов");
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.VALIDATION},
            description = "Register with password exactly 8 chars succeeds")
    public void registerWithPasswordExactly8Chars() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("Abcdefg8")
                .firstName("Test")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertCreated(response);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Register new user has capabilityTier exactly 1")
    public void registerNewUserCapabilityTierIs1() {
        Response response = registerUniqueUser();

        assertThat(response.jsonPath().getInt("capabilityTier")).isEqualTo(1);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Register with hyphenated name succeeds")
    public void registerWithHyphenatedNameSucceeds() {
        RegisterRequest request = TestDataFactory.registerRequestWithNames("Anne-Marie", "User");

        Response response = authClient.register(request);

        assertCreated(response);
        assertThat(response.jsonPath().getString("firstName")).isEqualTo("Anne-Marie");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Register with apostrophe in name succeeds")
    public void registerWithApostropheNameSucceeds() {
        RegisterRequest request = TestDataFactory.registerRequestWithNames("O'Brien", "User");

        Response response = authClient.register(request);

        assertCreated(response);
        assertThat(response.jsonPath().getString("firstName")).isEqualTo("O'Brien");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Register with dotted name succeeds")
    public void registerWithDottedNameSucceeds() {
        RegisterRequest request = TestDataFactory.registerRequestWithNames("Test", "Jr.");

        Response response = authClient.register(request);

        assertCreated(response);
        assertThat(response.jsonPath().getString("lastName")).isEqualTo("Jr.");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Register with spaced name succeeds")
    public void registerWithSpacedNameSucceeds() {
        RegisterRequest request = TestDataFactory.registerRequestWithNames("De La Cruz", "User");

        Response response = authClient.register(request);

        assertCreated(response);
        assertThat(response.jsonPath().getString("firstName")).isEqualTo("De La Cruz");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Register with accented name succeeds")
    public void registerWithAccentedNameSucceeds() {
        RegisterRequest request = TestDataFactory.registerRequestWithNames("José", "Müller");

        Response response = authClient.register(request);

        assertCreated(response);
        assertThat(response.jsonPath().getString("firstName")).isEqualTo("José");
        assertThat(response.jsonPath().getString("lastName")).isEqualTo("Müller");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Register with Arabic name succeeds")
    public void registerWithArabicNameSucceeds() {
        RegisterRequest request = TestDataFactory.registerRequestWithNames("محمد", "أحمد");

        Response response = authClient.register(request);

        assertCreated(response);
        assertThat(response.jsonPath().getString("firstName")).isEqualTo("محمد");
        assertThat(response.jsonPath().getString("lastName")).isEqualTo("أحمد");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Register with CJK name succeeds")
    public void registerWithCjkNameSucceeds() {
        RegisterRequest request = TestDataFactory.registerRequestWithNames("太郎", "山田");

        Response response = authClient.register(request);

        assertCreated(response);
        assertThat(response.jsonPath().getString("firstName")).isEqualTo("太郎");
        assertThat(response.jsonPath().getString("lastName")).isEqualTo("山田");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Register returns the same email in response")
    public void registerReturnsSameEmailInResponse() {
        RegisterRequest request = TestDataFactory.validRegisterRequest();

        Response response = authClient.register(request);

        assertCreated(response);
        assertThat(response.jsonPath().getString("email")).isEqualTo(request.getEmail());
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Register returns the same names in response")
    public void registerReturnsSameNamesInResponse() {
        RegisterRequest request = TestDataFactory.validRegisterRequest();

        Response response = authClient.register(request);

        assertCreated(response);
        assertThat(response.jsonPath().getString("firstName")).isEqualTo(request.getFirstName());
        assertThat(response.jsonPath().getString("lastName")).isEqualTo(request.getLastName());
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Register with 128 char password returns 400 (exceeds BCrypt 72-byte limit)")
    public void registerWithLongPassword() {
        String longPassword = TestDataFactory.longString(128);
        RegisterRequest request = TestDataFactory.registerRequestWithPassword(longPassword);

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.CONTRACT},
            description = "Register response Content-Type is application/json")
    public void registerResponseContentTypeIsJson() {
        Response response = registerUniqueUser();

        assertThat(response.getContentType()).contains("application/json");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Register with extra unknown fields succeeds")
    public void registerWithExtraUnknownFieldsSucceeds() {
        String json = """
                {"email":"%s","password":"password123","firstName":"Test","lastName":"User","unknownField":"value"}
                """.formatted(TestDataFactory.randomEmail());

        Response response = authClient.registerRaw(json);

        assertCreated(response);
    }

    // ==================== SCHEMA ====================

    @Test(groups = {TestGroup.SCHEMA},
            description = "Register response matches auth-user-response schema")
    public void registerResponseMatchesSchema() {
        Response response = registerUniqueUser();

        SchemaValidator.validateSchema(response, "auth-user-response.json");
    }

    // ==================== NEGATIVE / VALIDATION ====================

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Register with invalid email format returns 400")
    public void registerWithInvalidEmailFormatReturns400() {
        RegisterRequest request = RegisterRequest.builder()
                .email("not-an-email")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Register with short password returns 400")
    public void registerWithShortPasswordReturns400() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("short")
                .firstName("Test")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Register with whitespace-only name returns 400")
    public void registerWithWhitespaceOnlyNameReturns400() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("password123")
                .firstName("   ")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Register with null body returns 400")
    public void registerWithNullBodyReturns400() {
        Response response = authClient.postEmpty(Endpoint.AUTH_REGISTER);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Register with malformed JSON returns 400")
    public void registerWithMalformedJsonReturns400() {
        Response response = authClient.registerRaw("{invalid");

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Register with duplicate email returns 409")
    public void registerDuplicateEmailReturns409() {
        RegisterRequest request = TestDataFactory.validRegisterRequest();
        authClient.register(request);

        Response response = authClient.register(request);

        assertStatus(response, 409);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Register with password exactly 7 chars returns 400")
    public void registerWithPasswordExactly7CharsReturns400() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("Abcdef7")
                .firstName("Test")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Register with empty email returns 400")
    public void registerWithEmptyEmailReturns400() {
        RegisterRequest request = RegisterRequest.builder()
                .email("")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Register with empty password returns 400")
    public void registerWithEmptyPasswordReturns400() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("")
                .firstName("Test")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Register with empty firstName returns 400")
    public void registerWithEmptyFirstNameReturns400() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("password123")
                .firstName("")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Register with empty lastName returns 400")
    public void registerWithEmptyLastNameReturns400() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("password123")
                .firstName("Test")
                .lastName("")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Register with null email returns 400")
    public void registerWithNullEmailReturns400() {
        RegisterRequest request = RegisterRequest.builder()
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Register with null password returns 400")
    public void registerWithNullPasswordReturns400() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.randomEmail())
                .firstName("Test")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Register with null firstName returns 400")
    public void registerWithNullFirstNameReturns400() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("password123")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Register with null lastName returns 400")
    public void registerWithNullLastNameReturns400() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("password123")
                .firstName("Test")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Register with digits in name returns 400")
    public void registerWithNameContainingDigitsReturns400() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("password123")
                .firstName("John123")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Register with special chars in name returns 400")
    public void registerWithNameContainingSpecialCharsReturns400() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("password123")
                .firstName("John@#!")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Register with very long email returns 400")
    public void registerWithVeryLongEmailReturns400() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.longString(256) + "@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Register with very long name returns 400")
    public void registerWithVeryLongNameReturns400() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("password123")
                .firstName(TestDataFactory.longString(10001))
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Register with empty JSON object returns 400")
    public void registerWithEmptyJsonObjectReturns400() {
        Response response = authClient.registerRaw("{}");

        assertStatus(response, 400);
    }

    // ==================== CONTRACT ====================

    @Test(groups = {TestGroup.CONTRACT},
            description = "Register validation errors contain field and message")
    public void registerValidationErrorsFormat() {
        RegisterRequest request = TestDataFactory.invalidRegisterRequest();

        Response response = authClient.register(request);

        assertStatus(response, 400);
        String body = response.getBody().asString();
        assertThat(body).contains("field");
        assertThat(body).contains("message");
    }

    @Test(groups = {TestGroup.CONTRACT},
            description = "Register 400 matches ProblemDetail schema")
    public void registerProblemDetailSchemaOn400() {
        RegisterRequest request = TestDataFactory.invalidRegisterRequest();

        Response response = authClient.register(request);

        assertStatus(response, 400);
        SchemaValidator.validateProblemDetailSchema(response);
    }

    @Test(groups = {TestGroup.CONTRACT},
            description = "Register 409 matches ProblemDetail schema")
    public void registerProblemDetailSchemaOn409() {
        RegisterRequest request = TestDataFactory.validRegisterRequest();
        authClient.register(request);

        Response response = authClient.register(request);

        assertStatus(response, 409);
        SchemaValidator.validateProblemDetailSchema(response);
    }

    @Test(groups = {TestGroup.CONTRACT},
            description = "Register validation errors contain all invalid fields")
    public void registerValidationErrorsContainAllInvalidFields() {
        RegisterRequest request = TestDataFactory.invalidRegisterRequest();

        Response response = authClient.register(request);

        assertStatus(response, 400);
        String body = response.getBody().asString();
        assertThat(body).contains("email");
    }

    @Test(groups = {TestGroup.CONTRACT, TestGroup.METHOD_NOT_ALLOWED},
            description = "GET /register 405 matches ProblemDetail schema")
    public void register405MatchesProblemDetailSchema() {
        Response response = authClient.get(Endpoint.AUTH_REGISTER);

        assertStatus(response, 405);
        SchemaValidator.validateProblemDetailSchema(response);
    }

    // ==================== SECURITY ====================

    @Test(groups = {TestGroup.SECURITY},
            description = "Register with XSS in name fields is rejected with 400")
    public void registerWithXssInNameFields() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("password123")
                .firstName("<script>alert('xss')</script>")
                .lastName("<img src=x onerror=alert(1)>")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Register with SQL injection in email is rejected")
    public void registerWithSqlInjectionInEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test'; DROP TABLE users;--@kpi.ua")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Register with SQL injection in firstName is rejected")
    public void registerWithSqlInjectionInFirstName() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("password123")
                .firstName("Robert'; DROP TABLE users;--")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Register with empty/missing fields returns 400")
    public void registerEmptyFieldsReturns400() {
        RegisterRequest request = TestDataFactory.invalidRegisterRequest();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Register password is not returned in response body")
    public void registerPasswordNotReturnedInResponse() {
        String password = "SecurePassword123!";
        RegisterRequest request = RegisterRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password(password)
                .firstName("Test")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertCreated(response);
        assertThat(response.getBody().asString()).doesNotContain(password);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Register with XSS in email returns 400")
    public void registerWithXssInEmailReturns400() {
        RegisterRequest request = RegisterRequest.builder()
                .email("<script>alert('xss')</script>@test.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        Response response = authClient.register(request);

        assertStatus(response, 400);
    }

    // ==================== CONTENT-TYPE ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Register with XML content type returns 415")
    public void registerWithXmlContentTypeReturns415() {
        RegisterRequest request = TestDataFactory.validRegisterRequest();

        Response response = authClient.registerWithContentType(request, "application/xml");

        assertStatus(response, 415);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Register with form-encoded content type returns 415")
    public void registerWithFormEncodedReturns415() {
        RegisterRequest request = TestDataFactory.validRegisterRequest();

        Response response = authClient.registerWithContentType(request, "application/x-www-form-urlencoded");

        assertStatus(response, 415);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Register with plain text content type returns 415")
    public void registerWithPlainTextReturns415() {
        RegisterRequest request = TestDataFactory.validRegisterRequest();

        Response response = authClient.registerWithContentType(request, "text/plain");

        assertStatus(response, 415);
    }

    // ==================== METHOD_NOT_ALLOWED ====================

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "GET /register returns 405")
    public void registerMethodNotAllowedGet() {
        Response response = authClient.get(Endpoint.AUTH_REGISTER);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PUT /register returns 405")
    public void registerMethodNotAllowedPut() {
        RegisterRequest request = TestDataFactory.validRegisterRequest();
        Response response = authClient.put(Endpoint.AUTH_REGISTER, request);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "DELETE /register returns 405")
    public void registerMethodNotAllowedDelete() {
        Response response = authClient.delete(Endpoint.AUTH_REGISTER);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PATCH /register returns 405")
    public void registerMethodNotAllowedPatch() {
        RegisterRequest request = TestDataFactory.validRegisterRequest();
        Response response = authClient.patch(Endpoint.AUTH_REGISTER, request);

        assertStatus(response, 405);
    }
}
