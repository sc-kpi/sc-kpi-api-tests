package ua.kpi.sc.test.api.client.notification;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.model.notification.NotificationSettingsRequest;

public class NotificationSettingsClient extends ApiClient {

    @Step("GET /notifications/settings — Get notification settings")
    public Response getSettings(String authToken) {
        return get(Endpoint.NOTIFICATION_SETTINGS, authToken);
    }

    @Step("PUT /notifications/settings — Update notification settings")
    public Response updateSettings(NotificationSettingsRequest request, String authToken) {
        return put(Endpoint.NOTIFICATION_SETTINGS, request, authToken);
    }
}
