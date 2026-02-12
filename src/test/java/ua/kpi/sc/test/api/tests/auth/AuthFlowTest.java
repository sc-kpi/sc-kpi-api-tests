package ua.kpi.sc.test.api.tests.auth;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.auth.LoginRequest;
import ua.kpi.sc.test.api.model.auth.RegisterRequest;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Authentication")
@Feature("Auth Flows")
public class AuthFlowTest extends BaseAuthTest {

    // ==================== SMOKE ====================

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "Register -> getMe: user data matches registration request")
    public void fullRegistrationToMeFlow() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response regResponse = authClient.register(regRequest);
        assertCreated(regResponse);
        String accessToken = extractCookie(regResponse, "access_token");

        Response meResponse = authClient.getMe(accessToken);

        assertOk(meResponse);
        assertThat(meResponse.jsonPath().getString("email")).isEqualTo(regRequest.getEmail());
        assertThat(meResponse.jsonPath().getString("firstName")).isEqualTo(regRequest.getFirstName());
        assertThat(meResponse.jsonPath().getString("lastName")).isEqualTo(regRequest.getLastName());
    }

    // ==================== POSITIVE ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Register -> login -> getMe: email matches after login")
    public void fullLoginToMeFlow() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response regResponse = authClient.register(regRequest);
        assertCreated(regResponse);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();
        Response loginResponse = authClient.login(loginRequest);
        assertOk(loginResponse);
        String loginAccessToken = extractCookie(loginResponse, "access_token");

        Response meResponse = authClient.getMe(loginAccessToken);

        assertOk(meResponse);
        assertThat(meResponse.jsonPath().getString("email")).isEqualTo(regRequest.getEmail());
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Register -> refresh -> getMe with new token: email matches")
    public void fullRegistrationRefreshMeFlow() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response regResponse = authClient.register(regRequest);
        assertCreated(regResponse);
        String refreshToken = extractCookie(regResponse, "refresh_token");

        Response refreshResponse = authClient.refreshToken(refreshToken);
        assertOk(refreshResponse);
        String newAccessToken = extractCookie(refreshResponse, "access_token");

        Response meResponse = authClient.getMe(newAccessToken);

        assertOk(meResponse);
        assertThat(meResponse.jsonPath().getString("email")).isEqualTo(regRequest.getEmail());
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Register -> login -> refresh -> logout -> refresh with old token returns 401")
    public void fullLoginRefreshLogoutFlow() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response regResponse = authClient.register(regRequest);
        assertCreated(regResponse);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();
        Response loginResponse = authClient.login(loginRequest);
        assertOk(loginResponse);
        String loginRefreshToken = extractCookie(loginResponse, "refresh_token");

        Response refreshResponse = authClient.refreshToken(loginRefreshToken);
        assertOk(refreshResponse);
        String newAccessToken = extractCookie(refreshResponse, "access_token");

        Response logoutResponse = authClient.logout(newAccessToken);
        assertOk(logoutResponse);

        Response staleRefreshResponse = authClient.refreshToken(loginRefreshToken);
        assertStatus(staleRefreshResponse, 401);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Logout then login produces new tokens; new token works for getMe")
    public void logoutThenLoginProducesNewTokens() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response regResponse = authClient.register(regRequest);
        assertCreated(regResponse);
        String accessToken1 = extractCookie(regResponse, "access_token");

        authClient.logout(accessToken1);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();
        Response loginResponse = authClient.login(loginRequest);
        assertOk(loginResponse);
        String accessToken2 = extractCookie(loginResponse, "access_token");

        assertThat(accessToken2).isNotEqualTo(accessToken1);

        Response meResponse = authClient.getMe(accessToken2);
        assertOk(meResponse);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Register -> login -> refresh with login refresh token returns 200 with user data")
    public void refreshAfterLoginUsesLoginRefreshToken() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response regResponse = authClient.register(regRequest);
        assertCreated(regResponse);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();
        Response loginResponse = authClient.login(loginRequest);
        assertOk(loginResponse);
        String loginRefreshToken = extractCookie(loginResponse, "refresh_token");

        Response refreshResponse = authClient.refreshToken(loginRefreshToken);

        assertOk(refreshResponse);
        assertThat(refreshResponse.jsonPath().getString("email")).isNotNull();
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Register user A and user B -> getMe returns different ids and emails")
    public void concurrentRegistrationsDifferentUsers() {
        RegisterRequest regRequestA = TestDataFactory.validRegisterRequest();
        Response regResponseA = authClient.register(regRequestA);
        assertCreated(regResponseA);
        String tokenA = extractCookie(regResponseA, "access_token");

        RegisterRequest regRequestB = TestDataFactory.validRegisterRequest();
        Response regResponseB = authClient.register(regRequestB);
        assertCreated(regResponseB);
        String tokenB = extractCookie(regResponseB, "access_token");

        Response meA = authClient.getMe(tokenA);
        Response meB = authClient.getMe(tokenB);

        assertOk(meA);
        assertOk(meB);
        assertThat(meA.jsonPath().getString("id")).isNotEqualTo(meB.jsonPath().getString("id"));
        assertThat(meA.jsonPath().getString("email")).isNotEqualTo(meB.jsonPath().getString("email"));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Multiple refresh rotations preserve user data across 3 rotations")
    public void multipleRefreshRotationsPreserveUserData() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response regResponse = authClient.register(regRequest);
        assertCreated(regResponse);
        String email = regRequest.getEmail();
        String currentRefreshToken = extractCookie(regResponse, "refresh_token");
        String latestAccessToken = extractCookie(regResponse, "access_token");

        for (int i = 0; i < 3; i++) {
            Response refreshResponse = authClient.refreshToken(currentRefreshToken);
            assertOk(refreshResponse);
            assertThat(refreshResponse.jsonPath().getString("email")).isEqualTo(email);
            currentRefreshToken = extractCookie(refreshResponse, "refresh_token");
            latestAccessToken = extractCookie(refreshResponse, "access_token");
        }

        Response meResponse = authClient.getMe(latestAccessToken);
        assertOk(meResponse);
        assertThat(meResponse.jsonPath().getString("email")).isEqualTo(email);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Both register and login access tokens are valid for getMe")
    public void loginAfterRegistrationBothTokensWork() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response regResponse = authClient.register(regRequest);
        assertCreated(regResponse);
        String registerToken = extractCookie(regResponse, "access_token");

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();
        Response loginResponse = authClient.login(loginRequest);
        assertOk(loginResponse);
        String loginToken = extractCookie(loginResponse, "access_token");

        Response meWithRegToken = authClient.getMe(registerToken);
        assertOk(meWithRegToken);

        Response meWithLoginToken = authClient.getMe(loginToken);
        assertOk(meWithLoginToken);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Full cycle: register -> login -> getMe -> logout -> getMe returns 200 or 401")
    public void registerLoginMeLogoutFullCycle() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response regResponse = authClient.register(regRequest);
        assertCreated(regResponse);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();
        Response loginResponse = authClient.login(loginRequest);
        assertOk(loginResponse);
        String loginAccessToken = extractCookie(loginResponse, "access_token");

        Response meBeforeLogout = authClient.getMe(loginAccessToken);
        assertOk(meBeforeLogout);

        authClient.logout(loginAccessToken);

        Response meAfterLogout = authClient.getMe(loginAccessToken);
        assertThat(meAfterLogout.getStatusCode()).isIn(200, 401);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Refresh then getMe with new access token returns 200 and correct email")
    public void refreshThenGetMeWithNewAccessToken() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response regResponse = authClient.register(regRequest);
        assertCreated(regResponse);
        String refreshToken = extractCookie(regResponse, "refresh_token");

        Response refreshResponse = authClient.refreshToken(refreshToken);
        assertOk(refreshResponse);
        String newAccessToken = extractCookie(refreshResponse, "access_token");

        Response meResponse = authClient.getMe(newAccessToken);

        assertOk(meResponse);
        assertThat(meResponse.jsonPath().getString("email")).isEqualTo(regRequest.getEmail());
    }

    // ==================== SECURITY ====================

    @Test(groups = {TestGroup.POSITIVE, TestGroup.SECURITY},
            description = "Register -> login -> logout -> refresh with login refresh token returns 401")
    public void registerLoginLogoutCannotRefresh() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response regResponse = authClient.register(regRequest);
        assertCreated(regResponse);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();
        Response loginResponse = authClient.login(loginRequest);
        assertOk(loginResponse);
        String loginAccessToken = extractCookie(loginResponse, "access_token");
        String loginRefreshToken = extractCookie(loginResponse, "refresh_token");

        Response logoutResponse = authClient.logout(loginAccessToken);
        assertOk(logoutResponse);

        Response refreshResponse = authClient.refreshToken(loginRefreshToken);
        assertStatus(refreshResponse, 401);
    }
}
