package ua.kpi.sc.test.api.tests.auth.mfa;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.client.audit.AuditClient;
import ua.kpi.sc.test.api.client.featureflag.FeatureFlagClient;
import ua.kpi.sc.test.api.client.notification.NotificationAdminClient;
import ua.kpi.sc.test.api.client.user.UserClient;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.notification.BroadcastRequest;
import ua.kpi.sc.test.api.model.user.AssignPartnerLevelRequest;
import ua.kpi.sc.test.api.model.user.CreateUserRequest;
import ua.kpi.sc.test.api.model.user.UpdateStatusRequest;
import ua.kpi.sc.test.api.model.user.UpdateTierRequest;
import ua.kpi.sc.test.api.util.TotpHelper;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Authentication")
@Feature("TOTP 2FA Enforcement")
public class TotpEnforcementTest extends BaseTotpTest {

    private final UserClient userClient = new UserClient();
    private final FeatureFlagClient featureFlagClient = new FeatureFlagClient();
    private final NotificationAdminClient notificationAdminClient = new NotificationAdminClient();
    private final AuditClient auditClient = new AuditClient();

    @Test(groups = {TestGroup.SECURITY},
            description = "User management endpoints return 403 without 2FA enabled")
    public void userManagementEndpointsBlockedWithout2fa() {
        String token = registerAndGetAccessToken();
        String targetId = TestDataFactory.randomId();

        UpdateTierRequest tierReq = TestDataFactory.validUpdateTierRequest();
        assertThat(userClient.updateTier(targetId, tierReq, token).getStatusCode()).isEqualTo(403);

        UpdateStatusRequest statusReq = TestDataFactory.validUpdateStatusRequest(false);
        assertThat(userClient.updateStatus(targetId, statusReq, token).getStatusCode()).isEqualTo(403);

        assertThat(userClient.deleteUser(targetId, token).getStatusCode()).isEqualTo(403);

        CreateUserRequest createReq = TestDataFactory.validCreateUserRequest();
        assertThat(userClient.createUser(createReq, token).getStatusCode()).isEqualTo(403);

        AssignPartnerLevelRequest partnerReq = TestDataFactory.validAssignPartnerRequest();
        assertThat(userClient.assignPartnerLevel(targetId, partnerReq, token).getStatusCode()).isEqualTo(403);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Feature flag admin endpoints return 403 without 2FA enabled")
    public void featureFlagAdminBlockedWithout2fa() {
        String token = registerAndGetAccessToken();

        Response response = featureFlagClient.listFlags(token);
        assertStatus(response, 403);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Notification broadcast returns 403 without 2FA enabled")
    public void notificationBroadcastBlockedWithout2fa() {
        String token = registerAndGetAccessToken();

        BroadcastRequest request = BroadcastRequest.builder()
                .titleKey("test.title")
                .bodyKey("test.body")
                .category("SYSTEM")
                .build();
        Response response = notificationAdminClient.broadcast(token, request);
        assertStatus(response, 403);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Audit export returns 403 without 2FA enabled")
    public void auditExportBlockedWithout2fa() {
        String token = registerAndGetAccessToken();

        Response response = auditClient.exportCsv(token);
        assertStatus(response, 403);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "2FA-enabled user with BASIC tier is still blocked from admin endpoints")
    public void twoFactorEnabledButBasicTierStillBlocked() {
        TotpContext ctx = setupTotpForNewUser();

        String mfaToken = loginAndGetMfaToken(ctx.email(), ctx.password());
        String totpCode = TotpHelper.generateCode(ctx.secret());
        Response verifyResponse = totpClient.verifyLogin(mfaToken, totpCode);
        assertOk(verifyResponse);
        String mfaAccessToken = extractCookie(verifyResponse, "access_token");

        Response response = featureFlagClient.listFlags(mfaAccessToken);
        assertStatus(response, 403);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Verified TOTP login reflects twoFactorEnabled in /auth/me")
    public void verifiedLoginReflects2faEnabled() {
        TotpContext ctx = setupTotpForNewUser();

        String mfaToken = loginAndGetMfaToken(ctx.email(), ctx.password());
        String totpCode = TotpHelper.generateCode(ctx.secret());
        Response verifyResponse = totpClient.verifyLogin(mfaToken, totpCode);
        assertOk(verifyResponse);
        String mfaAccessToken = extractCookie(verifyResponse, "access_token");

        Response meResponse = authClient.getMe(mfaAccessToken);
        assertOk(meResponse);
        assertThat(meResponse.jsonPath().getBoolean("twoFactorEnabled")).isTrue();
    }
}
