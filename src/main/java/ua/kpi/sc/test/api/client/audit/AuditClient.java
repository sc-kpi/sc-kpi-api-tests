package ua.kpi.sc.test.api.client.audit;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Endpoint;

public class AuditClient extends ApiClient {

    @Step("GET /admin/audit-logs — Get audit logs")
    public Response getAuditLogs(String authToken) {
        return get(Endpoint.AUDIT_LOGS, authToken);
    }

    @Step("GET /admin/audit-logs with query params")
    public Response getAuditLogs(String authToken, String entityType, String action) {
        return requestSpec(authToken)
                .queryParam("entityType", entityType)
                .queryParam("action", action)
                .get(Endpoint.AUDIT_LOGS);
    }
}
