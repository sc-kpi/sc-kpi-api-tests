package ua.kpi.sc.test.api.client.featureflag;

import java.util.Map;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.model.featureflag.BulkToggleRequest;
import ua.kpi.sc.test.api.model.featureflag.CreateFeatureFlagRequest;
import ua.kpi.sc.test.api.model.featureflag.CreateOverrideRequest;
import ua.kpi.sc.test.api.model.featureflag.ToggleFeatureFlagRequest;
import ua.kpi.sc.test.api.model.featureflag.UpdateFeatureFlagRequest;

public class FeatureFlagClient extends ApiClient {

    // ==================== Public Evaluation ====================

    @Step("GET /feature-flags — Evaluate all flags (unauthenticated)")
    public Response evaluateAll() {
        return get(Endpoint.FEATURE_FLAGS);
    }

    @Step("GET /feature-flags — Evaluate all flags (authenticated)")
    public Response evaluateAll(String authToken) {
        return get(Endpoint.FEATURE_FLAGS, authToken);
    }

    @Step("GET /feature-flags/{key} — Evaluate single flag: {key}")
    public Response evaluate(String key) {
        return get(Endpoint.FEATURE_FLAGS + "/" + key);
    }

    @Step("GET /feature-flags/{key} — Evaluate single flag: {key} (authenticated)")
    public Response evaluate(String key, String authToken) {
        return get(Endpoint.FEATURE_FLAGS + "/" + key, authToken);
    }

    // ==================== Admin CRUD ====================

    @Step("GET /admin/feature-flags — List all flags")
    public Response listFlags(String authToken) {
        return get(Endpoint.ADMIN_FEATURE_FLAGS, authToken);
    }

    @Step("GET /admin/feature-flags — List flags with pagination")
    public Response listFlags(String authToken, Map<String, String> queryParams) {
        RequestSpecification spec = requestSpec(authToken);
        queryParams.forEach(spec::queryParam);
        return spec.get(Endpoint.ADMIN_FEATURE_FLAGS);
    }

    @Step("POST /admin/feature-flags — Create feature flag")
    public Response createFlag(CreateFeatureFlagRequest request, String authToken) {
        return post(Endpoint.ADMIN_FEATURE_FLAGS, request, authToken);
    }

    @Step("GET /admin/feature-flags/{id} — Get flag by ID: {id}")
    public Response getFlag(String id, String authToken) {
        return get(Endpoint.ADMIN_FEATURE_FLAGS + "/" + id, authToken);
    }

    @Step("PATCH /admin/feature-flags/{id} — Update flag: {id}")
    public Response updateFlag(String id, UpdateFeatureFlagRequest request, String authToken) {
        return patch(Endpoint.ADMIN_FEATURE_FLAGS + "/" + id, request, authToken);
    }

    @Step("PATCH /admin/feature-flags/{id}/toggle — Toggle flag: {id}")
    public Response toggleFlag(String id, ToggleFeatureFlagRequest request, String authToken) {
        return patch(Endpoint.ADMIN_FEATURE_FLAGS + "/" + id + "/toggle", request, authToken);
    }

    @Step("DELETE /admin/feature-flags/{id} — Delete flag: {id}")
    public Response deleteFlag(String id, String authToken) {
        return delete(Endpoint.ADMIN_FEATURE_FLAGS + "/" + id, authToken);
    }

    // ==================== Overrides ====================

    @Step("POST /admin/feature-flags/{id}/overrides — Add override to flag: {id}")
    public Response addOverride(String id, CreateOverrideRequest request, String authToken) {
        return post(Endpoint.ADMIN_FEATURE_FLAGS + "/" + id + "/overrides", request, authToken);
    }

    @Step("DELETE /admin/feature-flags/{id}/overrides/{overrideId} — Remove override")
    public Response removeOverride(String id, String overrideId, String authToken) {
        return delete(Endpoint.ADMIN_FEATURE_FLAGS + "/" + id + "/overrides/" + overrideId, authToken);
    }

    // ==================== Bulk Operations ====================

    @Step("POST /admin/feature-flags/bulk-toggle — Bulk toggle flags")
    public Response bulkToggle(BulkToggleRequest request, String authToken) {
        return post(Endpoint.ADMIN_FEATURE_FLAG_BULK_TOGGLE, request, authToken);
    }

}
