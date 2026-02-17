package ua.kpi.sc.test.api.client.ratelimit;

import java.util.Map;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.model.ratelimit.CreateRateLimitRuleRequest;
import ua.kpi.sc.test.api.model.ratelimit.RateLimitToggleRequest;
import ua.kpi.sc.test.api.model.ratelimit.UpdateRateLimitRuleRequest;

public class RateLimitClient extends ApiClient {

    // ==================== Admin CRUD ====================

    @Step("GET /admin/rate-limits — List rate limit rules")
    public Response listRules(String authToken) {
        return get(Endpoint.ADMIN_RATE_LIMITS, authToken);
    }

    @Step("GET /admin/rate-limits — List rate limit rules with pagination")
    public Response listRules(String authToken, Map<String, String> queryParams) {
        RequestSpecification spec = requestSpec(authToken);
        queryParams.forEach(spec::queryParam);
        return spec.get(Endpoint.ADMIN_RATE_LIMITS);
    }

    @Step("POST /admin/rate-limits — Create rate limit rule")
    public Response createRule(CreateRateLimitRuleRequest request, String authToken) {
        return post(Endpoint.ADMIN_RATE_LIMITS, request, authToken);
    }

    @Step("GET /admin/rate-limits/{id} — Get rule by ID: {id}")
    public Response getRule(String id, String authToken) {
        return get(Endpoint.ADMIN_RATE_LIMITS + "/" + id, authToken);
    }

    @Step("PATCH /admin/rate-limits/{id} — Update rule: {id}")
    public Response updateRule(String id, UpdateRateLimitRuleRequest request, String authToken) {
        return patch(Endpoint.ADMIN_RATE_LIMITS + "/" + id, request, authToken);
    }

    @Step("PATCH /admin/rate-limits/{id}/toggle — Toggle rule: {id}")
    public Response toggleRule(String id, RateLimitToggleRequest request, String authToken) {
        return patch(Endpoint.ADMIN_RATE_LIMITS + "/" + id + "/toggle", request, authToken);
    }

    @Step("DELETE /admin/rate-limits/{id} — Delete rule: {id}")
    public Response deleteRule(String id, String authToken) {
        return delete(Endpoint.ADMIN_RATE_LIMITS + "/" + id, authToken);
    }

    // ==================== Stats ====================

    @Step("GET /admin/rate-limits/stats — Get rate limit stats")
    public Response getStats(String authToken) {
        return get(Endpoint.ADMIN_RATE_LIMIT_STATS, authToken);
    }
}
