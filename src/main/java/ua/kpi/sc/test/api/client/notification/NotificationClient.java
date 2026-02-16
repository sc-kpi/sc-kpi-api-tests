package ua.kpi.sc.test.api.client.notification;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.model.notification.MarkReadRequest;
import ua.kpi.sc.test.api.model.notification.UpdatePreferencesRequest;

public class NotificationClient extends ApiClient {

    @Step("GET /notifications — Get notifications")
    public Response getNotifications(String authToken) {
        return get(Endpoint.NOTIFICATIONS, authToken);
    }

    @Step("GET /notifications — Get notifications with filters category={category}, read={read}")
    public Response getNotifications(String authToken, String category, Boolean read) {
        RequestSpecification spec = requestSpec(authToken);
        if (category != null) {
            spec.queryParam("category", category);
        }
        if (read != null) {
            spec.queryParam("read", read);
        }
        return spec.get(Endpoint.NOTIFICATIONS);
    }

    @Step("GET /notifications/unread-count — Get unread notification count")
    public Response getUnreadCount(String authToken) {
        return get(Endpoint.NOTIFICATION_UNREAD_COUNT, authToken);
    }

    @Step("PATCH /notifications/{id}/read — Mark notification as read")
    public Response markAsRead(String authToken, String id) {
        String path = Endpoint.NOTIFICATION_READ.replace("{id}", id);
        return patchEmpty(path, authToken);
    }

    @Step("PATCH /notifications/mark-read — Mark batch of notifications as read")
    public Response markBatchAsRead(String authToken, MarkReadRequest request) {
        return patch(Endpoint.NOTIFICATION_MARK_READ, request, authToken);
    }

    @Step("PATCH /notifications/mark-all-read — Mark all notifications as read")
    public Response markAllAsRead(String authToken) {
        return patchEmpty(Endpoint.NOTIFICATION_MARK_ALL_READ, authToken);
    }

    @Step("GET /notifications/preferences — Get notification preferences")
    public Response getPreferences(String authToken) {
        return get(Endpoint.NOTIFICATION_PREFERENCES, authToken);
    }

    @Step("PUT /notifications/preferences — Update notification preferences")
    public Response updatePreferences(String authToken, UpdatePreferencesRequest request) {
        return put(Endpoint.NOTIFICATION_PREFERENCES, request, authToken);
    }

    @Step("GET /notifications/stream — Get notification SSE stream")
    public Response getStream(String authToken) {
        return get(Endpoint.NOTIFICATION_STREAM, authToken);
    }
}
