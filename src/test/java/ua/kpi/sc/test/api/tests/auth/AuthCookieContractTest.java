package ua.kpi.sc.test.api.tests.auth;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.auth.LoginRequest;
import ua.kpi.sc.test.api.model.auth.RegisterRequest;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Authentication")
@Feature("Cookie Security")
public class AuthCookieContractTest extends BaseAuthTest {

    // ==================== REGISTER COOKIES ====================

    @Test(groups = {TestGroup.SECURITY, TestGroup.CONTRACT},
            description = "Register cookies have Secure flag (may only be set over HTTPS)")
    public void registerCookiesHaveSecureFlag() {
        Response response = registerUniqueUser();

        Cookie accessToken = extractDetailedCookie(response, "access_token");
        Cookie refreshToken = extractDetailedCookie(response, "refresh_token");
        assertThat(accessToken).isNotNull();
        assertThat(refreshToken).isNotNull();
        assertThat(accessToken.isSecured()).isTrue();
        assertThat(refreshToken.isSecured()).isTrue();
    }

    @Test(groups = {TestGroup.SECURITY, TestGroup.CONTRACT},
            description = "Register cookies have SameSite=Lax attribute")
    public void registerCookiesHaveSameSiteLax() {
        Response response = registerUniqueUser();

        String setCookieHeader = response.getHeaders().getValues("Set-Cookie").toString();
        assertThat(setCookieHeader).containsIgnoringCase("SameSite=Lax");
    }

    @Test(groups = {TestGroup.CONTRACT},
            description = "Register cookies have Path=/")
    public void registerCookiesHavePathSlash() {
        Response response = registerUniqueUser();

        Cookie accessToken = extractDetailedCookie(response, "access_token");
        Cookie refreshToken = extractDetailedCookie(response, "refresh_token");
        assertThat(accessToken).isNotNull();
        assertThat(refreshToken).isNotNull();
        assertThat(accessToken.getPath()).isEqualTo("/");
        assertThat(refreshToken.getPath()).isEqualTo("/");
    }

    @Test(groups = {TestGroup.CONTRACT},
            description = "Register cookies have positive Max-Age")
    public void registerCookiesHavePositiveMaxAge() {
        Response response = registerUniqueUser();

        Cookie accessToken = extractDetailedCookie(response, "access_token");
        Cookie refreshToken = extractDetailedCookie(response, "refresh_token");
        assertThat(accessToken).isNotNull();
        assertThat(refreshToken).isNotNull();
        assertThat(accessToken.getMaxAge()).isGreaterThan(0);
        assertThat(refreshToken.getMaxAge()).isGreaterThan(0);
    }

    // ==================== LOGIN COOKIES ====================

    @Test(groups = {TestGroup.SECURITY, TestGroup.CONTRACT},
            description = "Login cookies have Secure flag (may only be set over HTTPS)")
    public void loginCookiesHaveSecureFlag() {
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
        assertThat(accessToken.isSecured()).isTrue();
        assertThat(refreshToken.isSecured()).isTrue();
    }

    @Test(groups = {TestGroup.SECURITY, TestGroup.CONTRACT},
            description = "Login cookies have SameSite=Lax attribute")
    public void loginCookiesHaveSameSiteLax() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        authClient.register(regRequest);
        LoginRequest loginRequest = LoginRequest.builder()
                .email(regRequest.getEmail())
                .password(regRequest.getPassword())
                .build();

        Response response = authClient.login(loginRequest);

        assertOk(response);
        String setCookieHeader = response.getHeaders().getValues("Set-Cookie").toString();
        assertThat(setCookieHeader).containsIgnoringCase("SameSite=Lax");
    }

    @Test(groups = {TestGroup.CONTRACT},
            description = "Login cookies have Path=/")
    public void loginCookiesHavePathSlash() {
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
        assertThat(accessToken.getPath()).isEqualTo("/");
        assertThat(refreshToken.getPath()).isEqualTo("/");
    }

    // ==================== REFRESH COOKIES ====================

    @Test(groups = {TestGroup.SECURITY, TestGroup.CONTRACT},
            description = "Refresh cookies have Secure flag (may only be set over HTTPS)")
    public void refreshCookiesHaveSecureFlag() {
        Response regResponse = registerUniqueUser();
        String refreshToken = extractCookie(regResponse, "refresh_token");

        Response response = authClient.refreshToken(refreshToken);

        assertOk(response);
        Cookie accessTokenCookie = extractDetailedCookie(response, "access_token");
        Cookie refreshTokenCookie = extractDetailedCookie(response, "refresh_token");
        assertThat(accessTokenCookie).isNotNull();
        assertThat(refreshTokenCookie).isNotNull();
        assertThat(accessTokenCookie.isSecured()).isTrue();
        assertThat(refreshTokenCookie.isSecured()).isTrue();
    }

    @Test(groups = {TestGroup.SECURITY, TestGroup.CONTRACT},
            description = "Refresh cookies have SameSite=Lax attribute")
    public void refreshCookiesHaveSameSiteLax() {
        Response regResponse = registerUniqueUser();
        String refreshToken = extractCookie(regResponse, "refresh_token");

        Response response = authClient.refreshToken(refreshToken);

        assertOk(response);
        String setCookieHeader = response.getHeaders().getValues("Set-Cookie").toString();
        assertThat(setCookieHeader).containsIgnoringCase("SameSite=Lax");
    }

    @Test(groups = {TestGroup.CONTRACT},
            description = "Refresh cookies have Path=/")
    public void refreshCookiesHavePathSlash() {
        Response regResponse = registerUniqueUser();
        String refreshToken = extractCookie(regResponse, "refresh_token");

        Response response = authClient.refreshToken(refreshToken);

        assertOk(response);
        Cookie accessTokenCookie = extractDetailedCookie(response, "access_token");
        Cookie refreshTokenCookie = extractDetailedCookie(response, "refresh_token");
        assertThat(accessTokenCookie).isNotNull();
        assertThat(refreshTokenCookie).isNotNull();
        assertThat(accessTokenCookie.getPath()).isEqualTo("/");
        assertThat(refreshTokenCookie.getPath()).isEqualTo("/");
    }

    // ==================== LOGOUT COOKIES ====================

    @Test(groups = {TestGroup.SECURITY, TestGroup.CONTRACT},
            description = "Logout cookies have Secure flag (may only be set over HTTPS)")
    public void logoutCookiesHaveSecureFlag() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.logout(accessToken);

        assertOk(response);
        Cookie accessTokenCookie = extractDetailedCookie(response, "access_token");
        Cookie refreshTokenCookie = extractDetailedCookie(response, "refresh_token");
        assertThat(accessTokenCookie).isNotNull();
        assertThat(refreshTokenCookie).isNotNull();
        assertThat(accessTokenCookie.isSecured()).isTrue();
        assertThat(refreshTokenCookie.isSecured()).isTrue();
    }

    @Test(groups = {TestGroup.SECURITY, TestGroup.CONTRACT},
            description = "Logout cookies have SameSite=Lax attribute")
    public void logoutCookiesHaveSameSiteLax() {
        Response regResponse = registerUniqueUser();
        String accessToken = extractCookie(regResponse, "access_token");

        Response response = authClient.logout(accessToken);

        assertOk(response);
        String setCookieHeader = response.getHeaders().getValues("Set-Cookie").toString();
        assertThat(setCookieHeader).containsIgnoringCase("SameSite=Lax");
    }
}
