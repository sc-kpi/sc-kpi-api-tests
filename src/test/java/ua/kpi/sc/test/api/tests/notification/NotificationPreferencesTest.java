package ua.kpi.sc.test.api.tests.notification;

import java.util.List;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.base.BaseApiTest;
import ua.kpi.sc.test.api.client.notification.NotificationClient;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.model.notification.UpdatePreferencesRequest;

import static org.hamcrest.Matchers.notNullValue;

@Epic("Notifications")
@Feature("Notification Preferences")
public class NotificationPreferencesTest extends BaseApiTest {

    private final NotificationClient notificationClient = new NotificationClient();

    // ==================== Regression ====================

    @Test(groups = {TestGroup.REGRESSION, TestGroup.POSITIVE},
            description = "Get default notification preferences returns 200 with array body")
    public void shouldGetDefaultPreferences() {
        Response response = notificationClient.getPreferences(authToken);

        assertOk(response);
        response.then()
                .body("$", notNullValue());
    }

    @Test(groups = {TestGroup.REGRESSION, TestGroup.POSITIVE},
            description = "Update notification preferences returns 200")
    public void shouldUpdatePreferences() {
        UpdatePreferencesRequest request = UpdatePreferencesRequest.builder()
                .preferences(List.of(
                        UpdatePreferencesRequest.PreferenceUpdate.builder()
                                .category("SYSTEM")
                                .channel("IN_APP")
                                .enabled(true)
                                .build()
                ))
                .build();

        Response response = notificationClient.updatePreferences(authToken, request);

        assertOk(response);
    }
}
