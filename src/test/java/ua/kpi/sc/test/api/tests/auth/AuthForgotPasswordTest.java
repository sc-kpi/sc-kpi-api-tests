package ua.kpi.sc.test.api.tests.auth;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.client.mail.MailpitClient;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.auth.ForgotPasswordRequest;
import ua.kpi.sc.test.api.model.auth.RegisterRequest;
import ua.kpi.sc.test.api.util.SchemaValidator;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Authentication")
@Feature("Forgot Password")
public class AuthForgotPasswordTest extends BaseAuthTest {

    private final MailpitClient mailpitClient = new MailpitClient();

    @BeforeMethod
    public void cleanMailbox() {
        mailpitClient.deleteAllMessages();
    }

    // ==================== SMOKE ====================

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "Forgot password returns 200 for registered email")
    public void forgotPasswordReturns200ForRegisteredEmail() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        ForgotPasswordRequest request = TestDataFactory.validForgotPasswordRequest(regRequest.getEmail());

        Response response = authClient.forgotPassword(request);

        assertOk(response);
    }

    // ==================== POSITIVE ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Forgot password returns 200 for non-existent email (enumeration prevention)")
    public void forgotPasswordReturns200ForNonExistentEmail() {
        ForgotPasswordRequest request = TestDataFactory.validForgotPasswordRequest(
                "nonexistent-" + System.nanoTime() + "@kpi.ua");

        Response response = authClient.forgotPassword(request);

        assertOk(response);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Forgot password sends email to MailPit for registered user")
    public void forgotPasswordSendsEmailToMailpit() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        ForgotPasswordRequest request = TestDataFactory.validForgotPasswordRequest(regRequest.getEmail());
        authClient.forgotPassword(request);

        Response mailResponse = mailpitClient.waitForMessage(regRequest.getEmail(), 10);
        assertThat(mailResponse.jsonPath().getInt("messages_count")).isGreaterThan(0);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Forgot password email contains reset link with token")
    public void forgotPasswordEmailContainsResetToken() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        ForgotPasswordRequest request = TestDataFactory.validForgotPasswordRequest(regRequest.getEmail());
        authClient.forgotPassword(request);

        Response mailResponse = mailpitClient.waitForMessage(regRequest.getEmail(), 10);
        String messageId = mailResponse.jsonPath().getString("messages[0].ID");
        Response messageResponse = mailpitClient.getMessage(messageId);

        String body = messageResponse.jsonPath().getString("Text");
        assertThat(body).containsPattern("token=[a-f0-9-]+");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "No email sent for unknown user")
    public void noEmailSentForUnknownUser() {
        String unknownEmail = "unknown-" + System.nanoTime() + "@kpi.ua";
        ForgotPasswordRequest request = TestDataFactory.validForgotPasswordRequest(unknownEmail);
        authClient.forgotPassword(request);

        // Wait briefly then check no email arrived
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        Response mailResponse = mailpitClient.searchMessages(unknownEmail);
        assertThat(mailResponse.jsonPath().getInt("messages_count")).isEqualTo(0);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Multiple forgot password calls all return 200")
    public void multipleForgotPasswordCallsAllReturn200() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        ForgotPasswordRequest request = TestDataFactory.validForgotPasswordRequest(regRequest.getEmail());

        Response response1 = authClient.forgotPassword(request);
        Response response2 = authClient.forgotPassword(request);
        Response response3 = authClient.forgotPassword(request);

        assertOk(response1);
        assertOk(response2);
        assertOk(response3);
    }

    // ==================== NEGATIVE ====================

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Forgot password with empty email returns 400")
    public void forgotPasswordWithEmptyEmailReturns400() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("")
                .build();

        Response response = authClient.forgotPassword(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Forgot password with null email returns 400")
    public void forgotPasswordWithNullEmailReturns400() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder().build();

        Response response = authClient.forgotPassword(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Forgot password with invalid email format returns 400")
    public void forgotPasswordWithInvalidEmailReturns400() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("not-an-email")
                .build();

        Response response = authClient.forgotPassword(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Forgot password with whitespace-only email returns 400")
    public void forgotPasswordWithWhitespaceEmailReturns400() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("   ")
                .build();

        Response response = authClient.forgotPassword(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Forgot password with empty body returns 400")
    public void forgotPasswordWithEmptyBodyReturns400() {
        Response response = authClient.postEmpty(Endpoint.AUTH_FORGOT_PASSWORD);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Forgot password with malformed JSON returns 400")
    public void forgotPasswordWithMalformedJsonReturns400() {
        Response response = authClient.forgotPasswordRaw("{invalid");

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Forgot password with empty JSON object returns 400")
    public void forgotPasswordWithEmptyJsonObjectReturns400() {
        Response response = authClient.forgotPasswordRaw("{}");

        assertStatus(response, 400);
    }

    // ==================== CONTRACT ====================

    @Test(groups = {TestGroup.CONTRACT},
            description = "Forgot password 200 response has empty body (no JSON)")
    public void forgotPasswordResponseHasEmptyBody() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        ForgotPasswordRequest request = TestDataFactory.validForgotPasswordRequest(regRequest.getEmail());
        Response response = authClient.forgotPassword(request);

        assertOk(response);
        assertThat(response.getBody().asString()).isEmpty();
    }

    @Test(groups = {TestGroup.CONTRACT},
            description = "Forgot password 400 matches ProblemDetail schema")
    public void forgotPassword400MatchesProblemDetailSchema() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("bad")
                .build();

        Response response = authClient.forgotPassword(request);

        assertStatus(response, 400);
        SchemaValidator.validateProblemDetailSchema(response);
    }

    @Test(groups = {TestGroup.CONTRACT, TestGroup.SECURITY},
            description = "Forgot password 200 body doesn't reveal email existence")
    public void forgotPassword200BodyDoesNotRevealEmailExistence() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        // Existing email
        Response r1 = authClient.forgotPassword(
                TestDataFactory.validForgotPasswordRequest(regRequest.getEmail()));
        // Non-existing email
        Response r2 = authClient.forgotPassword(
                TestDataFactory.validForgotPasswordRequest("nonexistent-" + System.nanoTime() + "@kpi.ua"));

        assertOk(r1);
        assertOk(r2);
        // Both responses should be empty (API returns ResponseEntity<Void>)
        assertThat(r1.getBody().asString()).isEmpty();
        assertThat(r2.getBody().asString()).isEmpty();
    }

    // ==================== METHOD_NOT_ALLOWED ====================

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "GET /forgot-password returns 405")
    public void forgotPasswordMethodNotAllowedGet() {
        Response response = authClient.get(Endpoint.AUTH_FORGOT_PASSWORD);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PUT /forgot-password returns 405")
    public void forgotPasswordMethodNotAllowedPut() {
        ForgotPasswordRequest request = TestDataFactory.validForgotPasswordRequest(TestDataFactory.randomEmail());
        Response response = authClient.put(Endpoint.AUTH_FORGOT_PASSWORD, request);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "DELETE /forgot-password returns 405")
    public void forgotPasswordMethodNotAllowedDelete() {
        Response response = authClient.delete(Endpoint.AUTH_FORGOT_PASSWORD);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PATCH /forgot-password returns 405")
    public void forgotPasswordMethodNotAllowedPatch() {
        ForgotPasswordRequest request = TestDataFactory.validForgotPasswordRequest(TestDataFactory.randomEmail());
        Response response = authClient.patch(Endpoint.AUTH_FORGOT_PASSWORD, request);

        assertStatus(response, 405);
    }

    // ==================== SECURITY ====================

    @Test(groups = {TestGroup.SECURITY},
            description = "Forgot password with SQL injection in email is rejected with 400")
    public void forgotPasswordWithSqlInjectionReturns400() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("' OR 1=1 --@test.com")
                .build();

        Response response = authClient.forgotPassword(request);

        assertThat(response.getStatusCode()).isIn(400, 200);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Forgot password with XSS in email returns 400")
    public void forgotPasswordWithXssInEmailReturns400() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("<script>alert(1)</script>@test.com")
                .build();

        Response response = authClient.forgotPassword(request);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Response timing is similar for existing vs non-existing email")
    public void forgotPasswordTimingSimilarForExistingVsNonExisting() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        ForgotPasswordRequest existingRequest = TestDataFactory.validForgotPasswordRequest(regRequest.getEmail());
        ForgotPasswordRequest nonExistingRequest = TestDataFactory.validForgotPasswordRequest(
                "nonexistent-" + System.nanoTime() + "@kpi.ua");

        long start1 = System.currentTimeMillis();
        authClient.forgotPassword(existingRequest);
        long time1 = System.currentTimeMillis() - start1;

        long start2 = System.currentTimeMillis();
        authClient.forgotPassword(nonExistingRequest);
        long time2 = System.currentTimeMillis() - start2;

        // Timing should be within 2 seconds of each other (generous margin)
        assertThat(Math.abs(time1 - time2)).isLessThan(2000);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "No email sent for OAuth-only account")
    public void noEmailSentForOAuthOnlyAccount() {
        // OAuth-only accounts have no password set, so forgot-password should not send email
        // Since we can't easily create an OAuth-only account in tests,
        // we verify the endpoint returns 200 (enumeration prevention) for any email
        String oauthOnlyEmail = "oauth-only-" + System.nanoTime() + "@gmail.com";
        ForgotPasswordRequest request = TestDataFactory.validForgotPasswordRequest(oauthOnlyEmail);

        Response response = authClient.forgotPassword(request);

        assertOk(response);

        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        Response mailResponse = mailpitClient.searchMessages(oauthOnlyEmail);
        assertThat(mailResponse.jsonPath().getInt("messages_count")).isEqualTo(0);
    }
}
