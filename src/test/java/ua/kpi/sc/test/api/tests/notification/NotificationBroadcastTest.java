package ua.kpi.sc.test.api.tests.notification;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.annotation.Authentication;
import ua.kpi.sc.test.api.base.BaseAdminApiTest;
import ua.kpi.sc.test.api.client.notification.NotificationAdminClient;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.NotificationTestHelper;
import ua.kpi.sc.test.api.model.notification.BroadcastRequest;

@Epic("Notifications")
@Feature("Notification Broadcast")
public class NotificationBroadcastTest extends BaseAdminApiTest {

    private final NotificationAdminClient adminClient = new NotificationAdminClient();

    // ==================== Regression ====================

    @Test(groups = {TestGroup.REGRESSION, TestGroup.POSITIVE},
            description = "Admin can broadcast a notification — returns 200")
    public void shouldBroadcast() {
        BroadcastRequest request = NotificationTestHelper.validBroadcastRequest();

        Response response = adminClient.broadcast(authToken, request);

        assertOk(response);
    }

    // ==================== Security ====================

    @Test(groups = {TestGroup.REGRESSION, TestGroup.SECURITY},
            description = "Non-admin user cannot broadcast notification — returns 403")
    @Authentication(tier = "BASIC")
    public void shouldRejectBroadcastForNonAdmin() {
        BroadcastRequest request = NotificationTestHelper.validBroadcastRequest();

        Response response = adminClient.broadcast(authToken, request);

        assertStatus(response, 403);
    }
}
