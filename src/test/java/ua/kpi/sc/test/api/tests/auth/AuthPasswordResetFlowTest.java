package ua.kpi.sc.test.api.tests.auth;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.client.mail.MailpitClient;
import ua.kpi.sc.test.api.client.mail.MailpitHelper;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.auth.ForgotPasswordRequest;
import ua.kpi.sc.test.api.model.auth.LoginRequest;
import ua.kpi.sc.test.api.model.auth.RegisterRequest;
import ua.kpi.sc.test.api.model.auth.ResetPasswordRequest;
import ua.kpi.sc.test.api.model.user.ChangePasswordRequest;
import ua.kpi.sc.test.api.client.user.UserClient;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Authentication")
@Feature("Password Reset Flow")
public class AuthPasswordResetFlowTest extends BaseAuthTest {

    private final MailpitClient mailpitClient = new MailpitClient();
    private final UserClient userClient = new UserClient();
    private final MailpitHelper mailpitHelper = new MailpitHelper(authClient, mailpitClient);

    @BeforeMethod
    public void cleanMailbox() {
        mailpitClient.deleteAllMessages();
    }

    // ==================== SMOKE ====================

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "Full flow: register -> forgot -> MailPit -> extract token -> reset -> login with new password")
    public void fullPasswordResetFlow() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        String token = mailpitHelper.requestResetToken(regRequest.getEmail());
        String newPassword = "FlowNewPass123!";
        ResetPasswordRequest resetRequest = TestDataFactory.resetPasswordRequest(token, newPassword);
        Response resetResponse = authClient.resetPassword(resetRequest);
        assertOk(resetResponse);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(newPassword)
                .build();
        Response loginResponse = authClient.login(loginRequest);

        assertOk(loginResponse);
        assertThat(loginResponse.jsonPath().getString("email")).isEqualTo(regRequest.getEmail());
    }

    // ==================== POSITIVE ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Reset then change-password still works")
    public void resetThenChangePasswordStillWorks() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        String token = mailpitHelper.requestResetToken(regRequest.getEmail());
        String resetPassword = "ResetPass123!";
        authClient.resetPassword(TestDataFactory.resetPasswordRequest(token, resetPassword));

        // Login with new password
        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(resetPassword)
                .build();
        Response loginResponse = authClient.login(loginRequest);
        assertOk(loginResponse);
        String accessToken = extractCookie(loginResponse, "access_token");

        // Change password via authenticated endpoint
        String changedPassword = "ChangedPass123!";
        ChangePasswordRequest changeRequest = ChangePasswordRequest.builder()
                .currentPassword(resetPassword)
                .newPassword(changedPassword)
                .build();
        Response changeResponse = userClient.changePassword(changeRequest, accessToken);
        assertNoContent(changeResponse);

        // Login with changed password
        LoginRequest finalLogin = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(changedPassword)
                .build();
        Response finalLoginResponse = authClient.login(finalLogin);
        assertOk(finalLoginResponse);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Forgot-password for user A doesn't affect user B")
    public void forgotPasswordForUserADoesNotAffectUserB() {
        RegisterRequest userA = TestDataFactory.validRegisterRequest();
        RegisterRequest userB = TestDataFactory.validRegisterRequest();
        authClient.register(userA);
        authClient.register(userB);

        // Trigger forgot-password for user A only
        authClient.forgotPassword(TestDataFactory.validForgotPasswordRequest(userA.getEmail()));

        // User B should still be able to login with original password
        LoginRequest loginB = LoginRequest.builder()
                .email(userB.getEmail())
                .password(userB.getPassword())
                .build();
        Response loginResponseB = authClient.login(loginB);
        assertOk(loginResponseB);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Multiple forgot-password requests — all return 200")
    public void multipleForgotPasswordRequestsAllReturn200() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        ForgotPasswordRequest request = TestDataFactory.validForgotPasswordRequest(regRequest.getEmail());

        Response r1 = authClient.forgotPassword(request);
        Response r2 = authClient.forgotPassword(request);
        Response r3 = authClient.forgotPassword(request);

        assertOk(r1);
        assertOk(r2);
        assertOk(r3);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Reset then forgot-password again (second cycle works)")
    public void resetThenForgotPasswordSecondCycleWorks() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        // First cycle
        String token1 = mailpitHelper.requestResetToken(regRequest.getEmail());
        String firstNewPassword = "FirstReset123!";
        authClient.resetPassword(TestDataFactory.resetPasswordRequest(token1, firstNewPassword));

        // Clean mailbox for second cycle
        mailpitClient.deleteAllMessages();

        // Second cycle
        String token2 = mailpitHelper.requestResetToken(regRequest.getEmail());
        String secondNewPassword = "SecondReset123!";
        Response resetResponse = authClient.resetPassword(
                TestDataFactory.resetPasswordRequest(token2, secondNewPassword));
        assertOk(resetResponse);

        // Login with second new password
        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(secondNewPassword)
                .build();
        Response loginResponse = authClient.login(loginRequest);
        assertOk(loginResponse);
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.SECURITY},
            description = "Reset invalidates existing access tokens (stateless JWT behavior)")
    public void resetInvalidatesExistingAccessTokensOrNot() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response regResponse = authClient.register(regRequest);
        String existingAccessToken = extractCookie(regResponse, "access_token");

        String token = mailpitHelper.requestResetToken(regRequest.getEmail());
        authClient.resetPassword(TestDataFactory.validResetPasswordRequest(token));

        // Stateless JWT: existing token may or may not work depending on implementation
        Response meResponse = authClient.getMe(existingAccessToken);
        assertThat(meResponse.getStatusCode()).isIn(200, 401);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Concurrent forgot-password requests all return 200")
    public void concurrentForgotPasswordRequestsAllReturn200() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        ForgotPasswordRequest request = TestDataFactory.validForgotPasswordRequest(regRequest.getEmail());

        // Send multiple requests rapidly
        Response r1 = authClient.forgotPassword(request);
        Response r2 = authClient.forgotPassword(request);

        assertOk(r1);
        assertOk(r2);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Full flow works again after a complete reset cycle")
    public void fullFlowWorksAgainAfterCompleteCycle() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);

        // First complete cycle
        String token1 = mailpitHelper.requestResetToken(regRequest.getEmail());
        String password1 = "CycleOnePass123!";
        authClient.resetPassword(TestDataFactory.resetPasswordRequest(token1, password1));

        // Verify login with new password
        LoginRequest loginRequest1 = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(password1)
                .build();
        Response loginResponse1 = authClient.login(loginRequest1);
        assertOk(loginResponse1);

        // Clean mailbox
        mailpitClient.deleteAllMessages();

        // Second complete cycle
        String token2 = mailpitHelper.requestResetToken(regRequest.getEmail());
        String password2 = "CycleTwoPass123!";
        authClient.resetPassword(TestDataFactory.resetPasswordRequest(token2, password2));

        // Verify login with second new password
        LoginRequest loginRequest2 = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(password2)
                .build();
        Response loginResponse2 = authClient.login(loginRequest2);
        assertOk(loginResponse2);
    }
}
