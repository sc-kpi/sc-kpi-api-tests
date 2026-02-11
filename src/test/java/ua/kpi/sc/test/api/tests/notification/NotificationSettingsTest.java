package ua.kpi.sc.test.api.tests.notification;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.base.BaseAuthenticatedApiTest;
import ua.kpi.sc.test.api.config.TestGroup;

@Epic("Notifications")
@Feature("Notification Settings API")
public class NotificationSettingsTest extends BaseAuthenticatedApiTest {

    @Test(enabled = false, groups = {TestGroup.POSITIVE},
            description = "Get notification settings returns 200")
    public void getNotificationSettings() {
        // TODO: Implement when /api/v1/notifications/settings GET is ready
    }

    @Test(enabled = false, groups = {TestGroup.POSITIVE},
            description = "Update notification settings returns 200")
    public void updateNotificationSettings() {
        // TODO: Implement when /api/v1/notifications/settings PUT is ready
    }
}
