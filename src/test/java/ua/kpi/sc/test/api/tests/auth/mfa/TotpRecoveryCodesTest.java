package ua.kpi.sc.test.api.tests.auth.mfa;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.util.TotpHelper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Authentication")
@Feature("TOTP 2FA Recovery Codes")
public class TotpRecoveryCodesTest extends BaseTotpTest {

    // ==================== SMOKE ====================

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "Regenerate recovery codes returns 200 with new codes")
    public void regenerateRecoveryCodesReturnsNewCodes() {
        TotpContext ctx = setupTotpForNewUser();

        String code = TotpHelper.generateCode(ctx.secret());
        Response response = totpClient.regenerateRecoveryCodes(ctx.accessToken(), code);

        assertOk(response);
        List<String> newCodes = response.jsonPath().getList("codes", String.class);
        assertThat(newCodes).isNotEmpty();
    }

    // ==================== POSITIVE ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Regenerated codes are different from original codes")
    public void regeneratedCodesAreDifferentFromOriginal() {
        TotpContext ctx = setupTotpForNewUser();

        String code = TotpHelper.generateCode(ctx.secret());
        Response response = totpClient.regenerateRecoveryCodes(ctx.accessToken(), code);

        assertOk(response);
        List<String> newCodes = response.jsonPath().getList("codes", String.class);
        assertThat(newCodes).doesNotContainAnyElementsOf(ctx.recoveryCodes());
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Old recovery codes are invalidated after regeneration")
    public void oldRecoveryCodesInvalidatedAfterRegeneration() {
        TotpContext ctx = setupTotpForNewUser();

        String code = TotpHelper.generateCode(ctx.secret());
        Response regenResponse = totpClient.regenerateRecoveryCodes(ctx.accessToken(), code);
        assertOk(regenResponse);

        String mfaToken = loginAndGetMfaToken(ctx.email(), ctx.password());
        String oldRecoveryCode = ctx.recoveryCodes().getFirst();
        Response verifyResponse = totpClient.verifyLogin(mfaToken, oldRecoveryCode);

        assertStatus(verifyResponse, 401);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "New recovery codes work for login verification after regeneration")
    public void newRecoveryCodesWorkForLoginVerification() {
        TotpContext ctx = setupTotpForNewUser();

        String code = TotpHelper.generateCode(ctx.secret());
        Response regenResponse = totpClient.regenerateRecoveryCodes(ctx.accessToken(), code);
        assertOk(regenResponse);
        List<String> newCodes = regenResponse.jsonPath().getList("codes", String.class);

        String mfaToken = loginAndGetMfaToken(ctx.email(), ctx.password());
        Response verifyResponse = totpClient.verifyLogin(mfaToken, newCodes.getFirst());

        assertOk(verifyResponse);
        assertThat(verifyResponse.jsonPath().getString("email")).isEqualTo(ctx.email());
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Regenerated recovery codes have the same count as original")
    public void regeneratedCodesSameCountAsOriginal() {
        TotpContext ctx = setupTotpForNewUser();

        String code = TotpHelper.generateCode(ctx.secret());
        Response response = totpClient.regenerateRecoveryCodes(ctx.accessToken(), code);

        assertOk(response);
        List<String> newCodes = response.jsonPath().getList("codes", String.class);
        assertThat(newCodes).hasSameSizeAs(ctx.recoveryCodes());
    }

    // ==================== NEGATIVE ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Regenerate recovery codes with invalid TOTP code returns 400 or 401")
    public void regenerateWithInvalidCodeReturnsError() {
        TotpContext ctx = setupTotpForNewUser();

        Response response = totpClient.regenerateRecoveryCodes(ctx.accessToken(), "000000");

        assertThat(response.getStatusCode()).isIn(400, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Regenerate recovery codes without auth returns 401")
    public void regenerateWithoutAuthReturns401() {
        Response response = totpClient.regenerateRecoveryCodes(null, "123456");

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Regenerate recovery codes when 2FA not enabled returns 400 or 404")
    public void regenerateWhen2faNotEnabledReturnsError() {
        String accessToken = registerAndGetAccessToken();

        Response response = totpClient.regenerateRecoveryCodes(accessToken, "123456");

        assertThat(response.getStatusCode()).isIn(400, 404);
    }
}
