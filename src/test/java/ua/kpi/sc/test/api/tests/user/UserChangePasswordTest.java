package ua.kpi.sc.test.api.tests.user;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.base.BasePublicApiTest;
import ua.kpi.sc.test.api.client.auth.AuthClient;
import ua.kpi.sc.test.api.client.user.UserClient;
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
}
