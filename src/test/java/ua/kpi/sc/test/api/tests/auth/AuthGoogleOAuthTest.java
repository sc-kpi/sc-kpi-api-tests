package ua.kpi.sc.test.api.tests.auth;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.config.TestGroup;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Authentication")
@Feature("Google OAuth")
public class AuthGoogleOAuthTest extends BaseAuthTest {

    // ==================== SMOKE ====================

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "Google OAuth redirect returns 302")
    public void googleOAuthRedirectReturns302() {
        Response response = authClient.googleOAuthRedirect();

        assertStatus(response, 302);
    }

    // ==================== POSITIVE — Redirect ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Google OAuth redirect Location contains accounts.google.com")
    public void googleOAuthRedirectLocationContainsGoogleAccounts() {
        Response response = authClient.googleOAuthRedirect();

        assertStatus(response, 302);
        String location = response.getHeader("Location");
        assertThat(location).contains("accounts.google.com");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Google OAuth redirect Location contains client_id parameter")
    public void googleOAuthRedirectLocationContainsClientId() {
        Response response = authClient.googleOAuthRedirect();

        assertStatus(response, 302);
        String location = response.getHeader("Location");
        assertThat(location).contains("client_id=");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Google OAuth redirect Location contains redirect_uri parameter")
    public void googleOAuthRedirectLocationContainsRedirectUri() {
        Response response = authClient.googleOAuthRedirect();

        assertStatus(response, 302);
        String location = response.getHeader("Location");
        assertThat(location).contains("redirect_uri=");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Google OAuth redirect Location contains response_type=code")
    public void googleOAuthRedirectLocationContainsResponseTypeCode() {
        Response response = authClient.googleOAuthRedirect();

        assertStatus(response, 302);
        String location = response.getHeader("Location");
        assertThat(location).contains("response_type=code");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Google OAuth redirect Location contains openid scope")
    public void googleOAuthRedirectLocationContainsOpenidScope() {
        Response response = authClient.googleOAuthRedirect();

        assertStatus(response, 302);
        String location = response.getHeader("Location");
        assertThat(location).contains("scope=").contains("openid");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Google OAuth redirect Location contains state parameter")
    public void googleOAuthRedirectLocationContainsState() {
        Response response = authClient.googleOAuthRedirect();

        assertStatus(response, 302);
        String location = response.getHeader("Location");
        assertThat(location).contains("state=");
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.SECURITY},
            description = "Google OAuth redirect sets oauth_state cookie")
    public void googleOAuthRedirectSetsStateCookie() {
        Response response = authClient.googleOAuthRedirect();

        assertStatus(response, 302);
        String stateCookie = response.getCookie("oauth_state");
        assertThat(stateCookie).isNotNull().isNotEmpty();
    }

    @Test(groups = {TestGroup.POSITIVE, TestGroup.SECURITY},
            description = "Google OAuth oauth_state cookie is HttpOnly")
    public void googleOAuthStateCookieIsHttpOnly() {
        Response response = authClient.googleOAuthRedirect();

        assertStatus(response, 302);
        var cookie = extractDetailedCookie(response, "oauth_state");
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
    }

    // ==================== NEGATIVE — Callback ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Google OAuth callback without state cookie returns error redirect")
    public void googleOAuthCallbackWithoutStateCookieReturnsError() {
        Response response = authClient.getNoRedirectWithQueryParamsAndCookie(
                Endpoint.AUTH_OAUTH2_CALLBACK_GOOGLE,
                java.util.Map.of("code", "test-code", "state", "test-state"),
                "oauth_state", "");

        assertThat(response.getStatusCode()).isIn(302, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Google OAuth callback with mismatched state returns error redirect")
    public void googleOAuthCallbackWithMismatchedStateReturnsError() {
        // First get a valid state cookie
        Response redirectResponse = authClient.googleOAuthRedirect();
        String stateCookie = redirectResponse.getCookie("oauth_state");

        Response response = authClient.googleOAuthCallback("test-code", "wrong-state", stateCookie);

        assertThat(response.getStatusCode()).isIn(302, 400);
        if (response.getStatusCode() == 302) {
            assertThat(response.getHeader("Location")).contains("error");
        }
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Google OAuth callback with invalid code returns error redirect")
    public void googleOAuthCallbackWithInvalidCodeReturnsError() {
        Response redirectResponse = authClient.googleOAuthRedirect();
        String stateCookie = redirectResponse.getCookie("oauth_state");
        // Extract state from redirect Location
        String location = redirectResponse.getHeader("Location");
        String state = extractQueryParam(location, "state");

        Response response = authClient.googleOAuthCallback("invalid-code", state, stateCookie);

        assertThat(response.getStatusCode()).isIn(302, 400);
        if (response.getStatusCode() == 302) {
            assertThat(response.getHeader("Location")).contains("error");
        }
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Google OAuth callback with empty code returns error")
    public void googleOAuthCallbackWithEmptyCodeReturnsError() {
        Response redirectResponse = authClient.googleOAuthRedirect();
        String stateCookie = redirectResponse.getCookie("oauth_state");
        String location = redirectResponse.getHeader("Location");
        String state = extractQueryParam(location, "state");

        Response response = authClient.googleOAuthCallback("", state, stateCookie);

        assertThat(response.getStatusCode()).isIn(302, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Google OAuth callback with empty state returns error")
    public void googleOAuthCallbackWithEmptyStateReturnsError() {
        Response redirectResponse = authClient.googleOAuthRedirect();
        String stateCookie = redirectResponse.getCookie("oauth_state");

        Response response = authClient.googleOAuthCallback("test-code", "", stateCookie);

        assertThat(response.getStatusCode()).isIn(302, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Google OAuth callback with no params returns error")
    public void googleOAuthCallbackWithNoParamsReturnsError() {
        Response response = authClient.getNoRedirect(Endpoint.AUTH_OAUTH2_CALLBACK_GOOGLE);

        assertThat(response.getStatusCode()).isIn(302, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.SECURITY},
            description = "Google OAuth callback with SQL injection in code returns error")
    public void googleOAuthCallbackWithSqlInjectionReturnsError() {
        Response redirectResponse = authClient.googleOAuthRedirect();
        String stateCookie = redirectResponse.getCookie("oauth_state");
        String location = redirectResponse.getHeader("Location");
        String state = extractQueryParam(location, "state");

        Response response = authClient.googleOAuthCallback("' OR 1=1 --", state, stateCookie);

        assertThat(response.getStatusCode()).isIn(302, 400);
    }

    // ==================== CONTRACT ====================

    @Test(groups = {TestGroup.CONTRACT},
            description = "302 response has no JSON body")
    public void googleOAuth302HasNoJsonBody() {
        Response response = authClient.googleOAuthRedirect();

        assertStatus(response, 302);
        assertThat(response.getBody().asString()).isEmpty();
    }

    @Test(groups = {TestGroup.CONTRACT},
            description = "Error redirect points to frontend URL")
    public void googleOAuthErrorRedirectPointsToFrontend() {
        Response redirectResponse = authClient.googleOAuthRedirect();
        String stateCookie = redirectResponse.getCookie("oauth_state");

        Response response = authClient.googleOAuthCallback("invalid-code", "wrong-state", stateCookie);

        if (response.getStatusCode() == 302) {
            String location = response.getHeader("Location");
            assertThat(location).isNotNull();
            // Error redirect should point to frontend login, not Google
            assertThat(location).doesNotContain("accounts.google.com");
        }
    }

    // ==================== METHOD_NOT_ALLOWED ====================

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "POST /oauth2/google returns 405")
    public void googleOAuthMethodNotAllowedPost() {
        Response response = authClient.postEmpty(Endpoint.AUTH_OAUTH2_GOOGLE);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PUT /oauth2/google returns 405")
    public void googleOAuthMethodNotAllowedPut() {
        Response response = authClient.put(Endpoint.AUTH_OAUTH2_GOOGLE, "{}");

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "DELETE /oauth2/google returns 405")
    public void googleOAuthMethodNotAllowedDelete() {
        Response response = authClient.delete(Endpoint.AUTH_OAUTH2_GOOGLE);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PATCH /oauth2/google returns 405")
    public void googleOAuthMethodNotAllowedPatch() {
        Response response = authClient.patch(Endpoint.AUTH_OAUTH2_GOOGLE, "{}");

        assertStatus(response, 405);
    }

    // ==================== Helper ====================

    private String extractQueryParam(String url, String paramName) {
        if (url == null) return "";
        String[] parts = url.split("[?&]");
        for (String part : parts) {
            if (part.startsWith(paramName + "=")) {
                return part.substring(paramName.length() + 1);
            }
        }
        return "";
    }
}
