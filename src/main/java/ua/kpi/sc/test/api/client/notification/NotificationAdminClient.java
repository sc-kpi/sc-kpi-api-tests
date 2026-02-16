package ua.kpi.sc.test.api.client.notification;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.model.notification.BroadcastRequest;

public class NotificationAdminClient extends ApiClient {

    @Step("GET /admin/notifications — Get all notifications")
    public Response getAll(String authToken) {
        return get(Endpoint.ADMIN_NOTIFICATIONS, authToken);
    }

    @Step("GET /admin/notifications — Get all notifications with filters category={category}, search={search}")
    public Response getAll(String authToken, String category, String search) {
        RequestSpecification spec = requestSpec(authToken);
        if (category != null) {
            spec.queryParam("category", category);
        }
        if (search != null) {
            spec.queryParam("search", search);
        }
        return spec.get(Endpoint.ADMIN_NOTIFICATIONS);
    }

    @Step("POST /admin/notifications/broadcast — Broadcast notification")
    public Response broadcast(String authToken, BroadcastRequest request) {
        return post(Endpoint.ADMIN_NOTIFICATION_BROADCAST, request, authToken);
    }

    @Step("GET /admin/notifications/stats — Get notification statistics")
    public Response getStats(String authToken) {
        return get(Endpoint.ADMIN_NOTIFICATION_STATS, authToken);
    }

    @Step("DELETE /admin/notifications/cleanup — Cleanup old notifications (days={days})")
    public Response cleanup(String authToken, int days) {
        return requestSpec(authToken)
                .queryParam("days", days)
                .delete(Endpoint.ADMIN_NOTIFICATION_CLEANUP);
    }
}
