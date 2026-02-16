package ua.kpi.sc.test.api.tests.notification;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.annotation.Authentication;
import ua.kpi.sc.test.api.base.BaseApiTest;
import ua.kpi.sc.test.api.client.notification.NotificationAdminClient;
import ua.kpi.sc.test.api.client.notification.NotificationClient;
import ua.kpi.sc.test.api.config.TestGroup;

@Epic("Notifications")
@Feature("Notification Security")
public class NotificationSecurityTest extends BaseApiTest {

    private final NotificationClient notificationClient = new NotificationClient();
    private final NotificationAdminClient adminClient = new NotificationAdminClient();

    // ==================== Security ====================

    @Test(groups = {TestGroup.REGRESSION, TestGroup.SECURITY},
            description = "Unauthenticated access to notifications returns 401")
    @Authentication(enabled = false)
    public void shouldReturn401ForUnauthenticated() {
        Response response = notificationClient.getNotifications(null);

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.REGRESSION, TestGroup.SECURITY},
            description = "Basic user access to admin notifications returns 403")
    public void shouldReturn403ForInsufficientTier() {
        Response response = adminClient.getStats(authToken);

        assertStatus(response, 403);
    }
}
