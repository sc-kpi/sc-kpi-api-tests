package ua.kpi.sc.test.api.tests.auth.mfa;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.model.auth.LoginRequest;
import ua.kpi.sc.test.api.util.TotpHelper;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Authentication")
@Feature("TOTP 2FA Disable")
public class TotpDisableTest extends BaseTotpTest {

    // ==================== SMOKE ====================

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "Disable TOTP with valid password and code returns 200")
    public void disableTotpWithValidCredentials() {
        TotpContext ctx = setupTotpForNewUser();

        String code = TotpHelper.generateCode(ctx.secret());
        Response response = totpClient.disableTotp(ctx.accessToken(), ctx.password(), code);

        assertOk(response);
    }

    // ==================== POSITIVE ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "After disabling TOTP, status shows enabled=false")
    public void afterDisableTotpStatusShowsDisabled() {
        TotpContext ctx = setupTotpForNewUser();

        String code = TotpHelper.generateCode(ctx.secret());
        Response disableResponse = totpClient.disableTotp(ctx.accessToken(), ctx.password(), code);
        assertOk(disableResponse);

        Response statusResponse = totpClient.getStatus(ctx.accessToken());

        assertOk(statusResponse);
        assertThat(statusResponse.jsonPath().getBoolean("enabled")).isFalse();
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "After disabling TOTP, login no longer requires 2FA")
    public void afterDisableTotpLoginDoesNotRequire2fa() {
        TotpContext ctx = setupTotpForNewUser();

        String code = TotpHelper.generateCode(ctx.secret());
        totpClient.disableTotp(ctx.accessToken(), ctx.password(), code);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(ctx.email())
                .password(ctx.password())
                .build();
        Response loginResponse = authClient.login(loginRequest);

        assertOk(loginResponse);
        assertThat(loginResponse.jsonPath().getBoolean("twoFactorRequired")).isFalse();
        assertThat(extractCookie(loginResponse, "access_token")).isNotNull();
    }

    // ==================== NEGATIVE ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Disable TOTP with wrong password returns 401 or 403")
    public void disableTotpWithWrongPasswordReturnsError() {
        TotpContext ctx = setupTotpForNewUser();

        String code = TotpHelper.generateCode(ctx.secret());
        Response response = totpClient.disableTotp(ctx.accessToken(), "WrongPassword1!", code);

        assertThat(response.getStatusCode()).isIn(401, 403);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Disable TOTP with invalid code returns 400 or 401")
    public void disableTotpWithInvalidCodeReturnsError() {
        TotpContext ctx = setupTotpForNewUser();

        Response response = totpClient.disableTotp(ctx.accessToken(), ctx.password(), "000000");

        assertThat(response.getStatusCode()).isIn(400, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Disable TOTP without auth returns 401")
    public void disableTotpWithoutAuthReturns401() {
        Response response = totpClient.disableTotp(null, "password", "123456");

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Disable TOTP when not enabled returns 400 or 404")
    public void disableTotpWhenNotEnabledReturnsError() {
        String accessToken = registerAndGetAccessToken();

        Response response = totpClient.disableTotp(accessToken, "Test@123456", "123456");

        assertThat(response.getStatusCode()).isIn(400, 404);
    }
}
