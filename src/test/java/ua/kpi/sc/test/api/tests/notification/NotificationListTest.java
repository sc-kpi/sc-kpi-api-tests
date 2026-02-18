package ua.kpi.sc.test.api.tests.notification;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.base.BaseAuthenticatedApiTest;
import ua.kpi.sc.test.api.client.notification.NotificationClient;
import ua.kpi.sc.test.api.config.TestGroup;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@Epic("Notifications")
@Feature("Notification List")
public class NotificationListTest extends BaseAuthenticatedApiTest {

    private final NotificationClient notificationClient = new NotificationClient();

    // ==================== Smoke ====================

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "Get notifications returns 200 with list response")
    public void shouldListNotifications() {
        Response response = notificationClient.getNotifications(authToken);

        assertOk(response);
        response.then()
                .body("content", notNullValue());
    }

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "Get unread count returns 200 with count field")
    public void shouldGetUnreadCount() {
        Response response = notificationClient.getUnreadCount(authToken);

        assertOk(response);
        response.then()
                .body("count", greaterThanOrEqualTo(0));
    }

    // ==================== Regression ====================

    @Test(groups = {TestGroup.REGRESSION, TestGroup.POSITIVE},
            description = "Filter notifications by category returns 200")
    public void shouldFilterByCategory() {
        Response response = notificationClient.getNotifications(authToken, "SYSTEM", null);

        assertOk(response);
        response.then()
                .body("content", notNullValue());
    }

    @Test(groups = {TestGroup.REGRESSION, TestGroup.POSITIVE},
            description = "Filter notifications by read status returns 200")
    public void shouldFilterByReadStatus() {
        Response response = notificationClient.getNotifications(authToken, null, false);

        assertOk(response);
        response.then()
                .body("content", notNullValue());
    }
}
