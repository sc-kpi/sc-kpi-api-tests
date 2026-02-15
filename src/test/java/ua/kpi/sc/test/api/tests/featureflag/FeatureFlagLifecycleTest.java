package ua.kpi.sc.test.api.tests.featureflag;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.client.audit.AuditClient;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.FeatureFlagTestHelper;
import ua.kpi.sc.test.api.model.featureflag.BulkToggleRequest;
import ua.kpi.sc.test.api.model.featureflag.CreateFeatureFlagRequest;
import ua.kpi.sc.test.api.model.featureflag.FeatureFlagResponse;
import ua.kpi.sc.test.api.model.featureflag.UpdateFeatureFlagRequest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@Epic("Feature Flags")
@Feature("Feature Flag Lifecycle")
public class FeatureFlagLifecycleTest extends BaseFeatureFlagTest {

    private final AuditClient auditClient = new AuditClient();

    @Test(groups = {TestGroup.REGRESSION},
            description = "Full lifecycle: create → toggle → add override → update → delete")
    public void fullLifecycle_createToggleAddOverrideUpdateDelete() {
        // Create
        CreateFeatureFlagRequest createReq = FeatureFlagTestHelper.validCreateRequest();
        Response createResponse = flagClient.createFlag(createReq, authToken);
        assertCreated(createResponse);
        String flagId = createResponse.jsonPath().getString("id");

        // Toggle off
        Response toggleResponse = flagClient.toggleFlag(flagId,
                FeatureFlagTestHelper.toggleRequest(false), authToken);
        assertOk(toggleResponse);
        toggleResponse.then().body("enabled", equalTo(false));

        // Add override
        Response overrideResponse = flagClient.addOverride(flagId,
                FeatureFlagTestHelper.tierOverride(3, true), authToken);
        assertCreated(overrideResponse);

        // Update
        UpdateFeatureFlagRequest updateReq = UpdateFeatureFlagRequest.builder()
                .name("Updated Lifecycle Flag")
                .description("Updated during lifecycle test")
                .build();
        Response updateResponse = flagClient.updateFlag(flagId, updateReq, authToken);
        assertOk(updateResponse);
        updateResponse.then().body("name", equalTo("Updated Lifecycle Flag"));

        // Verify audit log has entries (via centralized audit API)
        Response auditResponse = auditClient.getAuditLogs(authToken,
                Map.of("entityType", "FEATURE_FLAG", "entityId", flagId));
        assertOk(auditResponse);
        auditResponse.then().body("content.size()", greaterThanOrEqualTo(3));

        // Delete
        Response deleteResponse = flagClient.deleteFlag(flagId, authToken);
        assertNoContent(deleteResponse);

        // Verify gone
        Response getResponse = flagClient.getFlag(flagId, authToken);
        assertStatus(getResponse, 404);
    }

    @Test(groups = {TestGroup.REGRESSION},
            description = "Create flag and verify it appears in evaluation endpoint")
    public void createFlagAndVerifyInEvaluationEndpoint() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setEnabled(true);
        request.setRolloutPercentage(100);
        FeatureFlagResponse created = getFlagHelper().createFlag(request);

        Response evalResponse = flagClient.evaluateAll();

        assertOk(evalResponse);
        evalResponse.then()
                .body("'" + created.getKey() + "'", equalTo(true));
    }

    @Test(groups = {TestGroup.REGRESSION},
            description = "Toggle flag and verify evaluation changes")
    public void toggleFlagAndVerifyEvaluationChanges() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setEnabled(true);
        request.setRolloutPercentage(100);
        FeatureFlagResponse created = getFlagHelper().createFlag(request);

        // Verify enabled
        Response eval1 = flagClient.evaluate(created.getKey());
        assertOk(eval1);
        eval1.then().body("enabled", equalTo(true));

        // Toggle off
        flagClient.toggleFlag(created.getId(), FeatureFlagTestHelper.toggleRequest(false), authToken);

        // Verify disabled
        Response eval2 = flagClient.evaluate(created.getKey());
        assertOk(eval2);
        eval2.then().body("enabled", equalTo(false));
    }

    @Test(groups = {TestGroup.REGRESSION},
            description = "Add override and verify evaluation for targeted user")
    public void addOverrideAndVerifyEvaluationForTargetedUser() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setEnabled(false);
        request.setRolloutPercentage(100);
        FeatureFlagResponse created = getFlagHelper().createFlag(request);

        // Flag is disabled globally
        Response eval1 = flagClient.evaluate(created.getKey());
        eval1.then().body("enabled", equalTo(false));

        // Add tier override for admin tier (5) to enable it
        flagClient.addOverride(created.getId(),
                FeatureFlagTestHelper.tierOverride(5, true), authToken);

        // Authenticated admin should see override
        Response eval2 = flagClient.evaluate(created.getKey(), authToken);
        assertOk(eval2);
        eval2.then().body("enabled", equalTo(true));

        // Unauthenticated should still see disabled
        Response eval3 = flagClient.evaluate(created.getKey());
        assertOk(eval3);
        eval3.then().body("enabled", equalTo(false));
    }

    @Test(groups = {TestGroup.REGRESSION},
            description = "Bulk toggle multiple flags and verify all")
    public void bulkToggleMultipleFlagsAndVerifyAll() {
        FeatureFlagResponse flag1 = getFlagHelper().createDefaultFlag();
        FeatureFlagResponse flag2 = getFlagHelper().createDefaultFlag();

        // Verify both enabled
        flagClient.evaluate(flag1.getKey()).then().body("enabled", equalTo(true));
        flagClient.evaluate(flag2.getKey()).then().body("enabled", equalTo(true));

        // Bulk disable
        BulkToggleRequest bulkRequest = BulkToggleRequest.builder()
                .keys(List.of(flag1.getKey(), flag2.getKey()))
                .enabled(false)
                .reason("Bulk lifecycle test")
                .build();
        Response bulkResponse = flagClient.bulkToggle(bulkRequest, authToken);
        assertOk(bulkResponse);

        // Verify both disabled
        flagClient.evaluate(flag1.getKey()).then().body("enabled", equalTo(false));
        flagClient.evaluate(flag2.getKey()).then().body("enabled", equalTo(false));
    }

    @Test(groups = {TestGroup.REGRESSION},
            description = "Delete flag and verify gone from evaluation")
    public void deleteFlagAndVerifyGoneFromEvaluation() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setEnabled(true);
        request.setRolloutPercentage(100);
        Response createResponse = flagClient.createFlag(request, authToken);
        String flagId = createResponse.jsonPath().getString("id");
        String flagKey = createResponse.jsonPath().getString("key");

        // Verify exists in evaluation
        Response eval1 = flagClient.evaluate(flagKey);
        assertOk(eval1);

        // Delete
        flagClient.deleteFlag(flagId, authToken);

        // Verify gone from single evaluation (should 404 or return false)
        Response eval2 = flagClient.evaluate(flagKey);
        // Non-existent flags return 404
        assertStatus(eval2, 404);
    }

    @Test(groups = {TestGroup.REGRESSION},
            description = "Create flag with all fields and verify persistence")
    public void createFlagWithAllFieldsAndVerifyPersistence() {
        CreateFeatureFlagRequest request = CreateFeatureFlagRequest.builder()
                .key("test.lifecycle." + UUID.randomUUID().toString().substring(0, 8))
                .name("Full Fields Flag")
                .description("A comprehensive test flag with all fields set")
                .enabled(true)
                .environment("staging")
                .rolloutPercentage(75)
                .build();

        FeatureFlagResponse created = getFlagHelper().createFlag(request);

        Response response = flagClient.getFlag(created.getId(), authToken);

        assertOk(response);
        response.then()
                .body("key", equalTo(request.getKey()))
                .body("name", equalTo("Full Fields Flag"))
                .body("description", equalTo("A comprehensive test flag with all fields set"))
                .body("enabled", equalTo(true))
                .body("environment", equalTo("staging"))
                .body("rolloutPercentage", equalTo(75))
                .body("createdBy", notNullValue())
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());
    }

    @Test(groups = {TestGroup.REGRESSION},
            description = "Update flag preserves unchanged fields")
    public void updateFlagPreservesUnchangedFields() {
        CreateFeatureFlagRequest createReq = CreateFeatureFlagRequest.builder()
                .key("test.preserve." + UUID.randomUUID().toString().substring(0, 8))
                .name("Original Name")
                .description("Original description")
                .enabled(true)
                .environment("dev")
                .rolloutPercentage(80)
                .build();

        FeatureFlagResponse created = getFlagHelper().createFlag(createReq);

        // Update only the name
        UpdateFeatureFlagRequest updateReq = UpdateFeatureFlagRequest.builder()
                .name("Updated Name Only")
                .build();
        flagClient.updateFlag(created.getId(), updateReq, authToken);

        // Verify other fields preserved
        Response response = flagClient.getFlag(created.getId(), authToken);

        assertOk(response);
        response.then()
                .body("name", equalTo("Updated Name Only"))
                .body("description", equalTo("Original description"))
                .body("enabled", equalTo(true))
                .body("environment", equalTo("dev"))
                .body("rolloutPercentage", equalTo(80));
    }
}
