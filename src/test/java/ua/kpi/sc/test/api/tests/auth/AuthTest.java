package ua.kpi.sc.test.api.tests.auth;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.base.BasePublicApiTest;
import ua.kpi.sc.test.api.config.TestGroup;

@Epic("Authentication")
@Feature("Auth API")
public class AuthTest extends BasePublicApiTest {

    @Test(enabled = false, groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "Login with valid credentials returns token")
    public void loginWithValidCredentials() {
        // TODO: Implement when /api/v1/auth/login is ready
    }

    @Test(enabled = false, groups = {TestGroup.POSITIVE},
            description = "Register new user returns 201")
    public void registerNewUser() {
        // TODO: Implement when /api/v1/auth/register is ready
    }

    @Test(enabled = false, groups = {TestGroup.POSITIVE},
            description = "Refresh token returns new access token")
    public void refreshTokenReturnsNewToken() {
        // TODO: Implement when /api/v1/auth/refresh is ready
    }

    @Test(enabled = false, groups = {TestGroup.NEGATIVE},
            description = "Login with invalid credentials returns 401")
    public void loginWithInvalidCredentialsReturns401() {
        // TODO: Implement when /api/v1/auth/login is ready
    }

    @Test(enabled = false, groups = {TestGroup.NEGATIVE},
            description = "Register with duplicate email returns 409")
    public void registerDuplicateEmailReturns409() {
        // TODO: Implement when /api/v1/auth/register is ready
    }

    @Test(enabled = false, groups = {TestGroup.SECURITY},
            description = "Expired token returns 401")
    public void expiredTokenReturns401() {
        // TODO: Implement when auth is ready
    }
}
