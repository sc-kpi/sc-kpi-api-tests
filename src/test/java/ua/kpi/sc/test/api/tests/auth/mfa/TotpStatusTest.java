package ua.kpi.sc.test.api.tests.auth.mfa;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.TestGroup;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Authentication")
@Feature("TOTP 2FA Status")
public class TotpStatusTest extends BaseTotpTest {

    // ==================== SMOKE ====================

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "Get 2FA status for user without TOTP returns enabled=false")
    public void statusWithout2faReturnsDisabled() {
        String accessToken = registerAndGetAccessToken();

        Response response = totpClient.getStatus(accessToken);

        assertOk(response);
        assertThat(response.jsonPath().getBoolean("enabled")).isFalse();
    }

    // ==================== POSITIVE ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Get 2FA status after enabling TOTP returns enabled=true")
    public void statusAfterEnabling2faReturnsEnabled() {
        TotpContext ctx = setupTotpForNewUser();

        Response response = totpClient.getStatus(ctx.accessToken());

        assertOk(response);
        assertThat(response.jsonPath().getBoolean("enabled")).isTrue();
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Status returns recoveryCodesRemaining after enabling TOTP")
    public void statusReturnsRecoveryCodesRemaining() {
        TotpContext ctx = setupTotpForNewUser();

        Response response = totpClient.getStatus(ctx.accessToken());

        assertOk(response);
        assertThat(response.jsonPath().getInt("recoveryCodesRemaining")).isGreaterThan(0);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Status returns enabledAt timestamp after enabling TOTP")
    public void statusReturnsEnabledAtTimestamp() {
        TotpContext ctx = setupTotpForNewUser();

        Response response = totpClient.getStatus(ctx.accessToken());

        assertOk(response);
        assertThat(response.jsonPath().getString("enabledAt")).isNotBlank();
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Status without 2FA has recoveryCodesRemaining=0")
    public void statusWithout2faHasZeroRecoveryCodes() {
        String accessToken = registerAndGetAccessToken();

        Response response = totpClient.getStatus(accessToken);

        assertOk(response);
        assertThat(response.jsonPath().getInt("recoveryCodesRemaining")).isZero();
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Status without 2FA has null enabledAt")
    public void statusWithout2faHasNullEnabledAt() {
        String accessToken = registerAndGetAccessToken();

        Response response = totpClient.getStatus(accessToken);

        assertOk(response);
        assertThat(response.jsonPath().getString("enabledAt")).isNull();
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Recovery codes remaining decreases after using a recovery code")
    public void recoveryCodesRemainingDecreasesAfterUse() {
        TotpContext ctx = setupTotpForNewUser();

        Response statusBefore = totpClient.getStatus(ctx.accessToken());
        assertOk(statusBefore);
        int countBefore = statusBefore.jsonPath().getInt("recoveryCodesRemaining");

        String mfaToken = loginAndGetMfaToken(ctx.email(), ctx.password());
        String recoveryCode = ctx.recoveryCodes().getFirst();
        Response verifyResponse = totpClient.verifyLogin(mfaToken, recoveryCode);
        assertOk(verifyResponse);
        String newAccessToken = extractCookie(verifyResponse, "access_token");

        Response statusAfter = totpClient.getStatus(newAccessToken);
        assertOk(statusAfter);
        int countAfter = statusAfter.jsonPath().getInt("recoveryCodesRemaining");

        assertThat(countAfter).isEqualTo(countBefore - 1);
    }

    // ==================== NEGATIVE ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Get 2FA status without auth returns 401")
    public void statusWithoutAuthReturns401() {
        Response response = totpClient.getStatus(null);

        assertStatus(response, 401);
    }
}
