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
@Feature("TOTP 2FA Login Verification")
public class TotpLoginVerifyTest extends BaseTotpTest {

    // ==================== SMOKE ====================

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "Login with 2FA enabled returns mfa_token cookie, then verify-login succeeds")
    public void loginWith2faThenVerifyLogin() {
        TotpContext ctx = setupTotpForNewUser();

        String mfaToken = loginAndGetMfaToken(ctx.email(), ctx.password());
        assertThat(mfaToken).isNotBlank();

        String code = TotpHelper.generateCode(ctx.secret());
        Response verifyResponse = totpClient.verifyLogin(mfaToken, code);

        assertOk(verifyResponse);
        assertThat(verifyResponse.jsonPath().getString("email")).isEqualTo(ctx.email());
        assertThat(extractCookie(verifyResponse, "access_token")).isNotNull();
        assertThat(extractCookie(verifyResponse, "refresh_token")).isNotNull();
    }

    // ==================== POSITIVE ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Login with 2FA returns twoFactorRequired=true in response body")
    public void loginWith2faReturnsTwoFactorRequired() {
        TotpContext ctx = setupTotpForNewUser();

        LoginRequest loginRequest = LoginRequest.builder()
                .email(ctx.email())
                .password(ctx.password())
                .build();
        Response loginResponse = authClient.login(loginRequest);

        assertOk(loginResponse);
        assertThat(loginResponse.jsonPath().getBoolean("twoFactorRequired")).isTrue();
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Verify-login response contains user fields: id, email, firstName, lastName")
    public void verifyLoginReturnsUserFields() {
        TotpContext ctx = setupTotpForNewUser();

        String mfaToken = loginAndGetMfaToken(ctx.email(), ctx.password());
        String code = TotpHelper.generateCode(ctx.secret());
        Response verifyResponse = totpClient.verifyLogin(mfaToken, code);

        assertOk(verifyResponse);
        assertThat(verifyResponse.jsonPath().getString("id")).isNotBlank();
        assertThat(verifyResponse.jsonPath().getString("email")).isEqualTo(ctx.email());
        assertThat(verifyResponse.jsonPath().getString("firstName")).isNotBlank();
        assertThat(verifyResponse.jsonPath().getString("lastName")).isNotBlank();
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Verify-login with recovery code succeeds")
    public void verifyLoginWithRecoveryCode() {
        TotpContext ctx = setupTotpForNewUser();

        String mfaToken = loginAndGetMfaToken(ctx.email(), ctx.password());
        String recoveryCode = ctx.recoveryCodes().getFirst();
        Response verifyResponse = totpClient.verifyLogin(mfaToken, recoveryCode);

        assertOk(verifyResponse);
        assertThat(verifyResponse.jsonPath().getString("email")).isEqualTo(ctx.email());
    }

    // ==================== NEGATIVE ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Verify-login with invalid TOTP code returns 401")
    public void verifyLoginWithInvalidCodeReturns401() {
        TotpContext ctx = setupTotpForNewUser();

        String mfaToken = loginAndGetMfaToken(ctx.email(), ctx.password());
        Response verifyResponse = totpClient.verifyLogin(mfaToken, "000000");

        assertStatus(verifyResponse, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Verify-login without mfa_token cookie returns 401")
    public void verifyLoginWithoutMfaTokenReturns401() {
        Response verifyResponse = totpClient.verifyLogin(null, "123456");

        assertStatus(verifyResponse, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Verify-login with expired/invalid mfa_token returns 401")
    public void verifyLoginWithInvalidMfaTokenReturns401() {
        Response verifyResponse = totpClient.verifyLogin("invalid-token-value", "123456");

        assertStatus(verifyResponse, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Verify-login with empty code returns 400")
    public void verifyLoginWithEmptyCodeReturns400() {
        TotpContext ctx = setupTotpForNewUser();

        String mfaToken = loginAndGetMfaToken(ctx.email(), ctx.password());
        Response verifyResponse = totpClient.verifyLogin(mfaToken, "");

        assertStatus(verifyResponse, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE, TestGroup.SECURITY},
            description = "Used recovery code cannot be reused")
    public void usedRecoveryCodeCannotBeReused() {
        TotpContext ctx = setupTotpForNewUser();

        String mfaToken1 = loginAndGetMfaToken(ctx.email(), ctx.password());
        String recoveryCode = ctx.recoveryCodes().getFirst();
        Response firstUse = totpClient.verifyLogin(mfaToken1, recoveryCode);
        assertOk(firstUse);

        String mfaToken2 = loginAndGetMfaToken(ctx.email(), ctx.password());
        Response secondUse = totpClient.verifyLogin(mfaToken2, recoveryCode);

        assertStatus(secondUse, 401);
    }
}
