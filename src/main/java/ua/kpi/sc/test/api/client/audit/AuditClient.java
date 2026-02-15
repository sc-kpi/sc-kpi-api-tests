package ua.kpi.sc.test.api.client.audit;

import java.util.Map;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Endpoint;

public class AuditClient extends ApiClient {

    @Step("GET /admin/audit-logs — Get audit logs")
    public Response getAuditLogs(String authToken) {
        return get(Endpoint.AUDIT_LOGS, authToken);
    }

    @Step("GET /admin/audit-logs — Get audit logs with filters")
    public Response getAuditLogs(String authToken, Map<String, String> queryParams) {
        RequestSpecification spec = requestSpec(authToken);
        queryParams.forEach(spec::queryParam);
        return spec.get(Endpoint.AUDIT_LOGS);
    }

    @Step("GET /admin/audit-logs — Get audit logs filtered by entityType={entityType}, action={action}")
    public Response getAuditLogs(String authToken, String entityType, String action) {
        RequestSpecification spec = requestSpec(authToken);
        if (entityType != null) {
            spec.queryParam("entityType", entityType);
        }
        if (action != null) {
            spec.queryParam("action", action);
        }
        return spec.get(Endpoint.AUDIT_LOGS);
    }

    @Step("GET /admin/audit-logs/export — Export audit logs as CSV")
    public Response exportCsv(String authToken) {
        return requestSpec(authToken)
                .accept("text/csv")
                .get(Endpoint.AUDIT_LOGS_EXPORT);
    }

    @Step("GET /admin/audit-logs/export — Export audit logs with filters")
    public Response exportCsv(String authToken, Map<String, String> queryParams) {
        RequestSpecification spec = requestSpec(authToken);
        spec.accept("text/csv");
        queryParams.forEach(spec::queryParam);
        return spec.get(Endpoint.AUDIT_LOGS_EXPORT);
    }

    @Step("GET /admin/audit-logs/stats — Get audit statistics")
    public Response getStats(String authToken) {
        return get(Endpoint.AUDIT_LOGS_STATS, authToken);
    }
}
