package ua.kpi.sc.test.api.tests.user;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.base.BasePublicApiTest;
import ua.kpi.sc.test.api.client.auth.AuthClient;
import ua.kpi.sc.test.api.client.user.UserClient;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.auth.LoginRequest;
import ua.kpi.sc.test.api.model.auth.RegisterRequest;
import ua.kpi.sc.test.api.model.user.ChangePasswordRequest;

@Epic("User Management")
@Feature("Change Password")
public class UserChangePasswordTest extends BasePublicApiTest {

    private final AuthClient authClient = new AuthClient();
    private final UserClient userClient = new UserClient();

    private record UserCredentials(String token, String email, String password) {}

    private UserCredentials registerAndLogin() {
        RegisterRequest request = TestDataFactory.validRegisterRequest();
        authClient.register(request);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
        Response loginResponse = authClient.login(loginRequest);
        assertOk(loginResponse);
        String token = loginResponse.getCookie("access_token");

        return new UserCredentials(token, request.getEmail(), request.getPassword());
    }

    @Test(groups = {TestGroup.SMOKE},
            description = "Change password returns 204")
    public void changePasswordReturns204() {
        var creds = registerAndLogin();
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword(creds.password())
                .newPassword("NewPassword123!")
                .build();

        Response response = userClient.changePassword(request, creds.token());

        assertNoContent(response);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Can login with new password after change")
    public void canLoginWithNewPassword() {
        var creds = registerAndLogin();
        String newPassword = "BrandNewPass123!";
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword(creds.password())
                .newPassword(newPassword)
                .build();
        userClient.changePassword(request, creds.token());

        // Login with new password
        LoginRequest loginRequest = LoginRequest.builder()
                .email(creds.email())
                .password(newPassword)
                .build();
        Response loginResponse = authClient.login(loginRequest);

        assertOk(loginResponse);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Cannot login with old password after change")
    public void cannotLoginWithOldPassword() {
        var creds = registerAndLogin();
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword(creds.password())
                .newPassword("BrandNewPass123!")
                .build();
        userClient.changePassword(request, creds.token());

        // Login with old password should fail
        LoginRequest loginRequest = LoginRequest.builder()
                .email(creds.email())
                .password(creds.password())
                .build();
        Response loginResponse = authClient.login(loginRequest);

        assertStatus(loginResponse, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Wrong current password returns 400")
    public void wrongCurrentPasswordReturns400() {
        var creds = registerAndLogin();
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("totallyWrongPassword")
                .newPassword("NewPassword123!")
                .build();

        Response response = userClient.changePassword(request, creds.token());

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Short new password returns 400")
    public void shortNewPasswordReturns400() {
        var creds = registerAndLogin();
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword(creds.password())
                .newPassword("short")
                .build();

        Response response = userClient.changePassword(request, creds.token());

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Change password without auth returns 401")
    public void withoutAuthReturns401() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("anyPassword")
                .newPassword("NewPassword123!")
                .build();

        Response response = userClient.changePassword(request, null);

        assertStatus(response, 401);
    }

    // ==================== GAP COVERAGE ====================

    @Test(groups = {TestGroup.POSITIVE, TestGroup.VALIDATION},
            description = "8-char new password returns 204")
    public void eightCharNewPasswordReturns204() {
        var creds = registerAndLogin();
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword(creds.password())
                .newPassword("Ab1@defg")
                .build();

        Response response = userClient.changePassword(request, creds.token());

        assertNoContent(response);
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.VALIDATION},
            description = "72-char new password returns 204")
    public void maxLengthNewPasswordReturns204() {
        var creds = registerAndLogin();
        String longPassword = "A1@" + "a".repeat(69);
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword(creds.password())
                .newPassword(longPassword)
                .build();

        Response response = userClient.changePassword(request, creds.token());

        assertNoContent(response);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "73-char new password returns 400")
    public void tooLongNewPasswordReturns400() {
        var creds = registerAndLogin();
        String tooLongPassword = "A1@" + "a".repeat(70);
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword(creds.password())
                .newPassword(tooLongPassword)
                .build();

        Response response = userClient.changePassword(request, creds.token());

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Empty current password returns 400")
    public void emptyCurrentPasswordReturns400() {
        var creds = registerAndLogin();
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("")
                .newPassword("NewPassword123!")
                .build();

        Response response = userClient.changePassword(request, creds.token());

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Empty new password returns 400")
    public void emptyNewPasswordReturns400() {
        var creds = registerAndLogin();
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword(creds.password())
                .newPassword("")
                .build();

        Response response = userClient.changePassword(request, creds.token());

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.VALIDATION},
            description = "Null fields returns 400")
    public void nullFieldsReturns400() {
        var creds = registerAndLogin();
        ChangePasswordRequest request = ChangePasswordRequest.builder().build();

        Response response = userClient.changePassword(request, creds.token());

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Empty body returns 400")
    public void emptyBodyReturns400() {
        var creds = registerAndLogin();

        Response response = userClient.patchEmpty(Endpoint.USER_ME_PASSWORD, creds.token());

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Malformed JSON returns 400")
    public void malformedJsonReturns400() {
        var creds = registerAndLogin();

        Response response = userClient.patchRaw(Endpoint.USER_ME_PASSWORD, "{invalid", creds.token());

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PUT /me/password returns 405")
    public void putMethodNotAllowed() {
        var creds = registerAndLogin();
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword(creds.password())
                .newPassword("NewPassword123!")
                .build();

        Response response = userClient.put(Endpoint.USER_ME_PASSWORD, request, creds.token());

        assertStatus(response, 405);
    }
}
