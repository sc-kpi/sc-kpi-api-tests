package ua.kpi.sc.test.api.tests.auth;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.client.mail.MailpitClient;
import ua.kpi.sc.test.api.client.mail.MailpitHelper;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.auth.LoginRequest;
import ua.kpi.sc.test.api.model.auth.RegisterRequest;
import ua.kpi.sc.test.api.model.auth.ResetPasswordRequest;
import ua.kpi.sc.test.api.util.SchemaValidator;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Authentication")
@Feature("Reset Password")
public class AuthResetPasswordTest extends BaseAuthTest {

    private final MailpitClient mailpitClient = new MailpitClient();
    private final MailpitHelper mailpitHelper = new MailpitHelper(authClient, mailpitClient);

    // ==================== SMOKE ====================

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "Reset password with valid token returns 200")
    public void resetPasswordWithValidTokenReturns200() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        String token = mailpitHelper.requestResetToken(regRequest.getEmail());
        ResetPasswordRequest resetRequest = TestDataFactory.validResetPasswordRequest(token);

        Response response = authClient.resetPassword(resetRequest);

        assertOk(response);
    }

    // ==================== POSITIVE ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Login with new password works after reset")
    public void loginWithNewPasswordWorksAfterReset() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        String token = mailpitHelper.requestResetToken(regRequest.getEmail());
        String newPassword = "ResetPass123!";
        ResetPasswordRequest resetRequest = TestDataFactory.resetPasswordRequest(token, newPassword);
        authClient.resetPassword(resetRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(newPassword)
                .build();
        Response loginResponse = authClient.login(loginRequest);

        assertOk(loginResponse);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Old password fails after reset")
    public void oldPasswordFailsAfterReset() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        String token = mailpitHelper.requestResetToken(regRequest.getEmail());
        String newPassword = "ResetPass123!";
        ResetPasswordRequest resetRequest = TestDataFactory.resetPasswordRequest(token, newPassword);
        authClient.resetPassword(resetRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();
        Response loginResponse = authClient.login(loginRequest);

        assertStatus(loginResponse, 401);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Reset password invalidates all refresh tokens")
    public void resetPasswordInvalidatesRefreshTokens() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response regResponse = authClient.register(regRequest);
        String refreshToken = extractCookie(regResponse, "refresh_token");

        String token = mailpitHelper.requestResetToken(regRequest.getEmail());
        ResetPasswordRequest resetRequest = TestDataFactory.validResetPasswordRequest(token);
        authClient.resetPassword(resetRequest);

        Response refreshResponse = authClient.refreshToken(refreshToken);
        assertStatus(refreshResponse, 401);
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.VALIDATION},
            description = "Reset password with 8-char password succeeds")
    public void resetPasswordWith8CharPasswordSucceeds() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        String token = mailpitHelper.requestResetToken(regRequest.getEmail());
        ResetPasswordRequest resetRequest = TestDataFactory.resetPasswordRequest(token, "Ab1@defg");

        Response response = authClient.resetPassword(resetRequest);

        assertOk(response);
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.VALIDATION},
            description = "Reset password with 72-char password succeeds")
    public void resetPasswordWith72CharPasswordSucceeds() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        String token = mailpitHelper.requestResetToken(regRequest.getEmail());
        String longPassword = "A1@" + "a".repeat(69);
        ResetPasswordRequest resetRequest = TestDataFactory.resetPasswordRequest(token, longPassword);

        Response response = authClient.resetPassword(resetRequest);

        assertOk(response);
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.CONTRACT},
            description = "Reset password 200 response has empty body (no JSON)")
    public void resetPasswordResponseHasEmptyBody() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        String token = mailpitHelper.requestResetToken(regRequest.getEmail());
        ResetPasswordRequest resetRequest = TestDataFactory.validResetPasswordRequest(token);

        Response response = authClient.resetPassword(resetRequest);

        assertOk(response);
        assertThat(response.getBody().asString()).isEmpty();
    }

    // ==================== NEGATIVE ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Reset password with invalid token returns 400")
    public void resetPasswordWithInvalidTokenReturns400() {
        ResetPasswordRequest resetRequest = TestDataFactory.resetPasswordRequest(
                "invalid-token-value", "NewPassword123!");

        Response response = authClient.resetPassword(resetRequest);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Reset password with empty token returns 400")
    public void resetPasswordWithEmptyTokenReturns400() {
        ResetPasswordRequest resetRequest = TestDataFactory.resetPasswordRequest("", "NewPassword123!");

        Response response = authClient.resetPassword(resetRequest);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Reset password with null token returns 400")
    public void resetPasswordWithNullTokenReturns400() {
        ResetPasswordRequest resetRequest = ResetPasswordRequest.builder()
                .newPassword("NewPassword123!")
                .build();

        Response response = authClient.resetPassword(resetRequest);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Reset password with whitespace-only token returns 400")
    public void resetPasswordWithWhitespaceTokenReturns400() {
        ResetPasswordRequest resetRequest = TestDataFactory.resetPasswordRequest("   ", "NewPassword123!");

        Response response = authClient.resetPassword(resetRequest);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Reset password with empty new password returns 400")
    public void resetPasswordWithEmptyPasswordReturns400() {
        ResetPasswordRequest resetRequest = TestDataFactory.resetPasswordRequest("some-token", "");

        Response response = authClient.resetPassword(resetRequest);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Reset password with null new password returns 400")
    public void resetPasswordWithNullPasswordReturns400() {
        ResetPasswordRequest resetRequest = ResetPasswordRequest.builder()
                .token("some-token")
                .build();

        Response response = authClient.resetPassword(resetRequest);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Reset password with short password (7 chars) returns 400")
    public void resetPasswordWithShortPasswordReturns400() {
        ResetPasswordRequest resetRequest = TestDataFactory.resetPasswordRequest("some-token", "Short1!");

        Response response = authClient.resetPassword(resetRequest);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Reset password with too-long password (73 chars) returns 400")
    public void resetPasswordWithTooLongPasswordReturns400() {
        String longPassword = "A1@" + "a".repeat(70);
        ResetPasswordRequest resetRequest = TestDataFactory.resetPasswordRequest("some-token", longPassword);

        Response response = authClient.resetPassword(resetRequest);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Reset password with whitespace-only password returns 400")
    public void resetPasswordWithWhitespacePasswordReturns400() {
        ResetPasswordRequest resetRequest = TestDataFactory.resetPasswordRequest("some-token", "        ");

        Response response = authClient.resetPassword(resetRequest);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Reset password with reused token returns 400")
    public void resetPasswordWithReusedTokenReturns400() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        String token = mailpitHelper.requestResetToken(regRequest.getEmail());
        ResetPasswordRequest resetRequest = TestDataFactory.validResetPasswordRequest(token);
        authClient.resetPassword(resetRequest);

        // Reuse the same token
        ResetPasswordRequest secondRequest = TestDataFactory.resetPasswordRequest(token, "AnotherPass123!");
        Response response = authClient.resetPassword(secondRequest);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Reset password with empty body returns 400")
    public void resetPasswordWithEmptyBodyReturns400() {
        Response response = authClient.postEmpty(Endpoint.AUTH_RESET_PASSWORD);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Reset password with malformed JSON returns 400")
    public void resetPasswordWithMalformedJsonReturns400() {
        Response response = authClient.resetPasswordRaw("{invalid");

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Reset password with empty JSON object returns 400")
    public void resetPasswordWithEmptyJsonObjectReturns400() {
        Response response = authClient.resetPasswordRaw("{}");

        assertStatus(response, 400);
    }

    // ==================== CONTRACT ====================

    @Test(groups = {TestGroup.CONTRACT},
            description = "Reset password 400 matches ProblemDetail schema")
    public void resetPassword400MatchesProblemDetailSchema() {
        ResetPasswordRequest resetRequest = TestDataFactory.resetPasswordRequest(
                "invalid-token", "NewPassword123!");

        Response response = authClient.resetPassword(resetRequest);

        assertStatus(response, 400);
        SchemaValidator.validateProblemDetailSchema(response);
    }

    @Test(groups = {TestGroup.CONTRACT},
            description = "Reset password 400 contains meaningful detail")
    public void resetPassword400ContainsMeaningfulDetail() {
        ResetPasswordRequest resetRequest = TestDataFactory.resetPasswordRequest(
                "invalid-token", "NewPassword123!");

        Response response = authClient.resetPassword(resetRequest);

        assertStatus(response, 400);
        assertThat(response.jsonPath().getString("detail")).isNotNull().isNotEmpty();
    }

    // ==================== METHOD_NOT_ALLOWED ====================

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "GET /reset-password returns 405")
    public void resetPasswordMethodNotAllowedGet() {
        Response response = authClient.get(Endpoint.AUTH_RESET_PASSWORD);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PUT /reset-password returns 405")
    public void resetPasswordMethodNotAllowedPut() {
        ResetPasswordRequest request = TestDataFactory.resetPasswordRequest("token", "pass");
        Response response = authClient.put(Endpoint.AUTH_RESET_PASSWORD, request);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "DELETE /reset-password returns 405")
    public void resetPasswordMethodNotAllowedDelete() {
        Response response = authClient.delete(Endpoint.AUTH_RESET_PASSWORD);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PATCH /reset-password returns 405")
    public void resetPasswordMethodNotAllowedPatch() {
        ResetPasswordRequest request = TestDataFactory.resetPasswordRequest("token", "pass");
        Response response = authClient.patch(Endpoint.AUTH_RESET_PASSWORD, request);

        assertStatus(response, 405);
    }

    // ==================== SECURITY ====================

    @Test(groups = {TestGroup.SECURITY},
            description = "Reset password with SQL injection in token is rejected")
    public void resetPasswordWithSqlInjectionInTokenReturns400() {
        ResetPasswordRequest resetRequest = TestDataFactory.resetPasswordRequest(
                "' OR 1=1 --", "NewPassword123!");

        Response response = authClient.resetPassword(resetRequest);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Reset password with XSS in token returns 400")
    public void resetPasswordWithXssInTokenReturns400() {
        ResetPasswordRequest resetRequest = TestDataFactory.resetPasswordRequest(
                "<script>alert(1)</script>", "NewPassword123!");

        Response response = authClient.resetPassword(resetRequest);

        assertStatus(response, 400);
    }
}
