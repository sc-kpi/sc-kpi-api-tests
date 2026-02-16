package ua.kpi.sc.test.api.tests.auth.mfa;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.auth.RegisterRequest;
import ua.kpi.sc.test.api.util.TotpHelper;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Authentication")
@Feature("TOTP 2FA Setup")
public class TotpSetupTest extends BaseTotpTest {

    // ==================== SMOKE ====================

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "Setup TOTP returns 200 with qrCodeDataUri and manualEntryKey")
    public void setupTotpReturnsQrCodeAndSecret() {
        String accessToken = registerAndGetAccessToken();

        Response response = totpClient.setupTotp(accessToken);

        assertOk(response);
        assertThat(response.jsonPath().getString("qrCodeDataUri")).isNotBlank();
        assertThat(response.jsonPath().getString("manualEntryKey")).isNotBlank();
    }

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "Setup + verify TOTP returns 200 with recovery codes")
    public void setupAndVerifyTotpReturnsRecoveryCodes() {
        String accessToken = registerAndGetAccessToken();

        Response setupResponse = totpClient.setupTotp(accessToken);
        assertOk(setupResponse);
        String secret = setupResponse.jsonPath().getString("manualEntryKey");

        String code = TotpHelper.generateCode(secret);
        Response verifyResponse = totpClient.verifySetup(accessToken, code);

        assertOk(verifyResponse);
        assertThat(verifyResponse.jsonPath().getList("codes", String.class)).isNotEmpty();
    }

    // ==================== POSITIVE ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Setup TOTP returns qrCodeDataUri starting with data:image/png")
    public void setupTotpQrCodeIsDataUri() {
        String accessToken = registerAndGetAccessToken();

        Response response = totpClient.setupTotp(accessToken);

        assertOk(response);
        assertThat(response.jsonPath().getString("qrCodeDataUri")).startsWith("data:image/png");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Setup TOTP returns manualEntryKey that is a valid Base32 string")
    public void setupTotpManualEntryKeyIsBase32() {
        String accessToken = registerAndGetAccessToken();

        Response response = totpClient.setupTotp(accessToken);

        assertOk(response);
        String key = response.jsonPath().getString("manualEntryKey");
        assertThat(key).matches("^[A-Z2-7]+=*$");
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Verify setup returns at least 8 recovery codes")
    public void verifySetupReturnsMultipleRecoveryCodes() {
        String accessToken = registerAndGetAccessToken();

        Response setupResponse = totpClient.setupTotp(accessToken);
        assertOk(setupResponse);
        String secret = setupResponse.jsonPath().getString("manualEntryKey");

        String code = TotpHelper.generateCode(secret);
        Response verifyResponse = totpClient.verifySetup(accessToken, code);

        assertOk(verifyResponse);
        assertThat(verifyResponse.jsonPath().getList("codes", String.class)).hasSizeGreaterThanOrEqualTo(8);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Calling setup twice returns a new secret each time")
    public void setupTotpTwiceReturnsDifferentSecrets() {
        String accessToken = registerAndGetAccessToken();

        Response setup1 = totpClient.setupTotp(accessToken);
        assertOk(setup1);
        String secret1 = setup1.jsonPath().getString("manualEntryKey");

        Response setup2 = totpClient.setupTotp(accessToken);
        assertOk(setup2);
        String secret2 = setup2.jsonPath().getString("manualEntryKey");

        assertThat(secret1).isNotEqualTo(secret2);
    }

    // ==================== NEGATIVE ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Setup TOTP without auth returns 401")
    public void setupTotpWithoutAuthReturns401() {
        Response response = totpClient.setupTotp(null);

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Verify setup with invalid code returns 400")
    public void verifySetupWithInvalidCodeReturns400() {
        String accessToken = registerAndGetAccessToken();

        Response setupResponse = totpClient.setupTotp(accessToken);
        assertOk(setupResponse);

        Response verifyResponse = totpClient.verifySetup(accessToken, "000000");

        assertStatus(verifyResponse, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Verify setup without prior setup returns 400 or 404")
    public void verifySetupWithoutSetupReturnsError() {
        String accessToken = registerAndGetAccessToken();

        Response verifyResponse = totpClient.verifySetup(accessToken, "123456");

        assertThat(verifyResponse.getStatusCode()).isIn(400, 404);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Verify setup with empty code returns 400")
    public void verifySetupWithEmptyCodeReturns400() {
        String accessToken = registerAndGetAccessToken();

        totpClient.setupTotp(accessToken);

        Response verifyResponse = totpClient.verifySetup(accessToken, "");

        assertStatus(verifyResponse, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Verify setup with non-numeric code returns 400")
    public void verifySetupWithNonNumericCodeReturns400() {
        String accessToken = registerAndGetAccessToken();

        totpClient.setupTotp(accessToken);

        Response verifyResponse = totpClient.verifySetup(accessToken, "abcdef");

        assertStatus(verifyResponse, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Setup TOTP when already enabled returns 409 or 400")
    public void setupTotpWhenAlreadyEnabledReturnsConflict() {
        TotpContext ctx = setupTotpForNewUser();

        Response response = totpClient.setupTotp(ctx.accessToken());

        assertThat(response.getStatusCode()).isIn(400, 409);
    }
}
