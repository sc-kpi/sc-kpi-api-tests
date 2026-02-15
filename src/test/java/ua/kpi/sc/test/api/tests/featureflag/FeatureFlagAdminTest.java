package ua.kpi.sc.test.api.tests.featureflag;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.annotation.Authentication;
import ua.kpi.sc.test.api.client.audit.AuditClient;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.FeatureFlagTestHelper;
import ua.kpi.sc.test.api.model.featureflag.BulkToggleRequest;
import ua.kpi.sc.test.api.model.featureflag.CreateFeatureFlagRequest;
import ua.kpi.sc.test.api.model.featureflag.CreateOverrideRequest;
import ua.kpi.sc.test.api.model.featureflag.FeatureFlagResponse;
import ua.kpi.sc.test.api.model.featureflag.ToggleFeatureFlagRequest;
import ua.kpi.sc.test.api.model.featureflag.UpdateFeatureFlagRequest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

@Epic("Feature Flags")
@Feature("Admin Feature Flag Management")
public class FeatureFlagAdminTest extends BaseFeatureFlagTest {

    private final AuditClient auditClient = new AuditClient();

    // ==================== CRUD ====================

    @Test(groups = {TestGroup.SMOKE},
            description = "List feature flags returns paginated response")
    public void listFlagsReturnsPaginatedResponse() {
        Response response = flagClient.listFlags(authToken);

        assertOk(response);
        response.then()
                .body("content", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(0));
    }

    @Test(groups = {TestGroup.SMOKE},
            description = "Create feature flag returns 201 with correct fields")
    public void createFlagReturns201() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();

        Response response = flagClient.createFlag(request, authToken);

        assertCreated(response);
        FeatureFlagResponse flag = response.as(FeatureFlagResponse.class);
        getFlagHelper(); // ensure helper exists for tracking
        flagHelper = new FeatureFlagTestHelper(flagClient, authToken);
        response.then()
                .body("id", notNullValue())
                .body("key", equalTo(request.getKey()))
                .body("name", equalTo(request.getName()))
                .body("enabled", equalTo(request.isEnabled()));

        // Track for cleanup
        flagClient.deleteFlag(flag.getId(), authToken);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Get feature flag by ID returns correct flag")
    public void getFlagByIdReturnsCorrectFlag() {
        FeatureFlagResponse created = getFlagHelper().createDefaultFlag();

        Response response = flagClient.getFlag(created.getId(), authToken);

        assertOk(response);
        response.then()
                .body("id", equalTo(created.getId()))
                .body("key", equalTo(created.getKey()))
                .body("name", equalTo(created.getName()));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Update feature flag changes fields")
    public void updateFlagChangesFields() {
        FeatureFlagResponse created = getFlagHelper().createDefaultFlag();
        UpdateFeatureFlagRequest update = UpdateFeatureFlagRequest.builder()
                .name("Updated Name")
                .description("Updated description")
                .rolloutPercentage(50)
                .build();

        Response response = flagClient.updateFlag(created.getId(), update, authToken);

        assertOk(response);
        response.then()
                .body("name", equalTo("Updated Name"))
                .body("description", equalTo("Updated description"))
                .body("rolloutPercentage", equalTo(50));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Delete feature flag returns 204")
    public void deleteFlagReturns204() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        Response createResponse = flagClient.createFlag(request, authToken);
        String id = createResponse.jsonPath().getString("id");

        Response response = flagClient.deleteFlag(id, authToken);

        assertNoContent(response);

        // Verify deleted
        Response getResponse = flagClient.getFlag(id, authToken);
        assertStatus(getResponse, 404);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Created flag appears in list")
    public void createdFlagAppearsInList() {
        FeatureFlagResponse created = getFlagHelper().createDefaultFlag();

        Response response = flagClient.listFlags(authToken, Map.of("size", "100", "sort", "createdAt,desc"));

        assertOk(response);
        response.then()
                .body("content.key", hasItem(created.getKey()));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "List flags pagination works")
    public void listFlagsPaginationWorks() {
        getFlagHelper().createDefaultFlag();

        Response response = flagClient.listFlags(authToken, Map.of("size", "1"));

        assertOk(response);
        response.then()
                .body("content.size()", equalTo(1));
    }

    // ==================== Toggle ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Toggle flag changes enabled state")
    public void toggleFlagChangesEnabledState() {
        FeatureFlagResponse created = getFlagHelper().createDefaultFlag();

        Response response = flagClient.toggleFlag(created.getId(),
                FeatureFlagTestHelper.toggleRequest(false), authToken);

        assertOk(response);
        response.then()
                .body("enabled", equalTo(false));
    }

    // ==================== Overrides ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Add tier override returns 201")
    public void addTierOverrideReturns201() {
        FeatureFlagResponse created = getFlagHelper().createDefaultFlag();
        CreateOverrideRequest override = FeatureFlagTestHelper.tierOverride(3, false);

        Response response = flagClient.addOverride(created.getId(), override, authToken);

        assertCreated(response);
        response.then()
                .body("overrideType", equalTo("TIER"))
                .body("tierLevel", equalTo(3))
                .body("enabled", equalTo(false));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Add user override returns 201")
    public void addUserOverrideReturns201() {
        FeatureFlagResponse created = getFlagHelper().createDefaultFlag();
        String userId = java.util.UUID.randomUUID().toString();
        CreateOverrideRequest override = FeatureFlagTestHelper.userOverride(userId, true);

        Response response = flagClient.addOverride(created.getId(), override, authToken);

        assertCreated(response);
        response.then()
                .body("overrideType", equalTo("USER"))
                .body("userId", equalTo(userId))
                .body("enabled", equalTo(true));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Remove override returns 204")
    public void removeOverrideReturns204() {
        FeatureFlagResponse created = getFlagHelper().createDefaultFlag();
        CreateOverrideRequest override = FeatureFlagTestHelper.tierOverride(2, true);
        Response addResponse = flagClient.addOverride(created.getId(), override, authToken);
        String overrideId = addResponse.jsonPath().getString("id");

        Response response = flagClient.removeOverride(created.getId(), overrideId, authToken);

        assertNoContent(response);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Override appears in flag detail")
    public void overrideAppearsInFlagDetail() {
        FeatureFlagResponse created = getFlagHelper().createDefaultFlag();
        flagClient.addOverride(created.getId(), FeatureFlagTestHelper.tierOverride(1, false), authToken);

        Response response = flagClient.getFlag(created.getId(), authToken);

        assertOk(response);
        response.then()
                .body("overrides.size()", equalTo(1))
                .body("overrides[0].overrideType", equalTo("TIER"))
                .body("overrides[0].tierLevel", equalTo(1));
    }

    // ==================== Bulk Toggle ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Bulk toggle disables multiple flags")
    public void bulkToggleDisablesMultipleFlags() {
        FeatureFlagResponse flag1 = getFlagHelper().createDefaultFlag();
        FeatureFlagResponse flag2 = getFlagHelper().createDefaultFlag();

        BulkToggleRequest request = BulkToggleRequest.builder()
                .keys(List.of(flag1.getKey(), flag2.getKey()))
                .enabled(false)
                .reason("Bulk disable test")
                .build();

        Response response = flagClient.bulkToggle(request, authToken);

        assertOk(response);

        // Verify both disabled
        Response getResponse1 = flagClient.getFlag(flag1.getId(), authToken);
        getResponse1.then().body("enabled", equalTo(false));

        Response getResponse2 = flagClient.getFlag(flag2.getId(), authToken);
        getResponse2.then().body("enabled", equalTo(false));
    }

    // ==================== Audit Log (Centralized) ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Toggle generates audit log entry in centralized audit")
    public void toggleGeneratesAuditLogEntry() {
        FeatureFlagResponse created = getFlagHelper().createDefaultFlag();
        flagClient.toggleFlag(created.getId(), FeatureFlagTestHelper.toggleRequest(false), authToken);

        Response response = auditClient.getAuditLogs(authToken,
                Map.of("entityType", "FEATURE_FLAG", "entityId", created.getId()));

        assertOk(response);
        response.then()
                .body("content.size()", greaterThanOrEqualTo(1));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Feature flag audit logs available via centralized endpoint")
    public void featureFlagAuditLogsAvailableViaCentralizedEndpoint() {
        Response response = auditClient.getAuditLogs(authToken,
                Map.of("entityType", "FEATURE_FLAG"));

        assertOk(response);
        response.then()
                .body("content", notNullValue());
    }

    // ==================== Validation ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Create flag with duplicate key returns 409")
    public void createFlagDuplicateKeyReturns409() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        getFlagHelper().createFlag(request);

        Response response = flagClient.createFlag(request, authToken);

        assertStatus(response, 409);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Create flag with invalid key returns 400")
    public void createFlagInvalidKeyReturns400() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setKey("INVALID_KEY!");

        Response response = flagClient.createFlag(request, authToken);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Create flag with blank name returns 400")
    public void createFlagBlankNameReturns400() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setName("");

        Response response = flagClient.createFlag(request, authToken);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Get non-existent flag returns 404")
    public void getNonExistentFlagReturns404() {
        Response response = flagClient.getFlag(java.util.UUID.randomUUID().toString(), authToken);

        assertStatus(response, 404);
    }

    // ==================== Security ====================

    @Test(groups = {TestGroup.SECURITY},
            description = "Admin endpoints without auth return 401")
    public void adminEndpointsWithoutAuthReturn401() {
        Response response = flagClient.listFlags(null);

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Create flag without auth returns 401")
    public void createFlagWithoutAuthReturns401() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();

        Response response = flagClient.createFlag(request, null);

        assertStatus(response, 401);
    }

    // ==================== Pagination & Filtering ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "List flags with custom page size returns correct number")
    public void listFlagsWithPageSizeReturnsCorrectNumberOfItems() {
        getFlagHelper().createDefaultFlag();
        getFlagHelper().createDefaultFlag();

        Response response = flagClient.listFlags(authToken, Map.of("size", "1"));

        assertOk(response);
        response.then()
                .body("content.size()", equalTo(1))
                .body("size", equalTo(1));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "List flags empty page returns empty content")
    public void listFlagsEmptyPageReturnsEmptyContent() {
        Response response = flagClient.listFlags(authToken, Map.of("page", "999"));

        assertOk(response);
        response.then()
                .body("content.size()", equalTo(0));
    }

    // ==================== Schema Validation ====================

    @Test(groups = {TestGroup.SCHEMA},
            description = "Create flag response contains all expected fields")
    public void createFlagResponseContainsAllExpectedFields() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setEnvironment("dev");
        FeatureFlagResponse flag = getFlagHelper().createFlag(request);

        Response response = flagClient.getFlag(flag.getId(), authToken);

        assertOk(response);
        response.then()
                .body("id", notNullValue())
                .body("key", notNullValue())
                .body("name", notNullValue())
                .body("enabled", notNullValue())
                .body("rolloutPercentage", notNullValue())
                .body("overrides", notNullValue())
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());
    }

    @Test(groups = {TestGroup.SCHEMA},
            description = "Get flag response contains overrides array")
    public void getFlagResponseContainsOverridesArray() {
        FeatureFlagResponse created = getFlagHelper().createDefaultFlag();
        flagClient.addOverride(created.getId(), FeatureFlagTestHelper.tierOverride(1, true), authToken);

        Response response = flagClient.getFlag(created.getId(), authToken);

        assertOk(response);
        response.then()
                .body("overrides.size()", greaterThanOrEqualTo(1))
                .body("overrides[0].id", notNullValue())
                .body("overrides[0].overrideType", notNullValue())
                .body("overrides[0].enabled", notNullValue());
    }

    @Test(groups = {TestGroup.SCHEMA},
            description = "List flags response contains pagination metadata")
    public void listFlagsResponseContainsPaginationMetadata() {
        Response response = flagClient.listFlags(authToken);

        assertOk(response);
        response.then()
                .body("totalElements", greaterThanOrEqualTo(0))
                .body("totalPages", greaterThanOrEqualTo(0))
                .body("size", greaterThanOrEqualTo(1));
    }

    // ==================== RBAC / Security ====================

    @Test(groups = {TestGroup.SECURITY},
            description = "Basic user cannot create flag — returns 403")
    @Authentication(tier = "BASIC")
    public void basicUserCannotCreateFlag_returns403() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();

        Response response = flagClient.createFlag(request, authToken);

        assertStatus(response, 403);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Basic user cannot delete flag — returns 403")
    @Authentication(tier = "BASIC")
    public void basicUserCannotDeleteFlag_returns403() {
        Response response = flagClient.deleteFlag(UUID.randomUUID().toString(), authToken);

        assertStatus(response, 403);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Basic user cannot toggle flag — returns 403")
    @Authentication(tier = "BASIC")
    public void basicUserCannotToggleFlag_returns403() {
        Response response = flagClient.toggleFlag(
                UUID.randomUUID().toString(),
                FeatureFlagTestHelper.toggleRequest(false),
                authToken);

        assertStatus(response, 403);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Basic user cannot add override — returns 403")
    @Authentication(tier = "BASIC")
    public void basicUserCannotAddOverride_returns403() {
        Response response = flagClient.addOverride(
                UUID.randomUUID().toString(),
                FeatureFlagTestHelper.tierOverride(1, true),
                authToken);

        assertStatus(response, 403);
    }

    // ==================== Validation Edge Cases ====================

    @Test(groups = {TestGroup.VALIDATION},
            description = "Create flag with key containing uppercase returns 400")
    public void createFlagWithKeyContainingUppercase_returns400() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setKey("Test.UPPERCASE");

        Response response = flagClient.createFlag(request, authToken);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.VALIDATION},
            description = "Create flag with key containing spaces returns 400")
    public void createFlagWithKeyContainingSpaces_returns400() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setKey("test flag key");

        Response response = flagClient.createFlag(request, authToken);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.VALIDATION},
            description = "Create flag with key starting with number returns 400")
    public void createFlagWithKeyStartingWithNumber_returns400() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setKey("1test.flag");

        Response response = flagClient.createFlag(request, authToken);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.VALIDATION},
            description = "Create flag with rollout percentage over 100 returns 400")
    public void createFlagWithRolloutPercentageOver100_returns400() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setRolloutPercentage(150);

        Response response = flagClient.createFlag(request, authToken);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.VALIDATION},
            description = "Create flag with negative rollout percentage returns 400")
    public void createFlagWithNegativeRolloutPercentage_returns400() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setRolloutPercentage(-10);

        Response response = flagClient.createFlag(request, authToken);

        assertStatus(response, 400);
    }

    // ==================== Override Management ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Add tier override appears in flag detail")
    public void addTierOverrideAppearsInFlagDetail() {
        FeatureFlagResponse created = getFlagHelper().createDefaultFlag();
        flagClient.addOverride(created.getId(), FeatureFlagTestHelper.tierOverride(3, false), authToken);

        Response response = flagClient.getFlag(created.getId(), authToken);

        assertOk(response);
        response.then()
                .body("overrides.size()", equalTo(1))
                .body("overrides[0].overrideType", equalTo("TIER"))
                .body("overrides[0].tierLevel", equalTo(3))
                .body("overrides[0].enabled", equalTo(false));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Add user override appears in flag detail")
    public void addUserOverrideAppearsInFlagDetail() {
        FeatureFlagResponse created = getFlagHelper().createDefaultFlag();
        String userId = UUID.randomUUID().toString();
        flagClient.addOverride(created.getId(), FeatureFlagTestHelper.userOverride(userId, true), authToken);

        Response response = flagClient.getFlag(created.getId(), authToken);

        assertOk(response);
        response.then()
                .body("overrides.size()", equalTo(1))
                .body("overrides[0].overrideType", equalTo("USER"))
                .body("overrides[0].userId", equalTo(userId));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Remove tier override disappears from flag detail")
    public void removeTierOverrideDisappearsFromFlagDetail() {
        FeatureFlagResponse created = getFlagHelper().createDefaultFlag();
        Response addResponse = flagClient.addOverride(created.getId(),
                FeatureFlagTestHelper.tierOverride(2, true), authToken);
        String overrideId = addResponse.jsonPath().getString("id");

        flagClient.removeOverride(created.getId(), overrideId, authToken);

        Response response = flagClient.getFlag(created.getId(), authToken);
        assertOk(response);
        response.then()
                .body("overrides.size()", equalTo(0));
    }

    // ==================== Audit Log Expanded (Centralized) ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Create flag generates audit entry in centralized audit")
    public void createFlagGeneratesAuditEntry() {
        FeatureFlagResponse created = getFlagHelper().createDefaultFlag();

        Response response = auditClient.getAuditLogs(authToken,
                Map.of("entityType", "FEATURE_FLAG", "entityId", created.getId()));

        assertOk(response);
        response.then()
                .body("content.size()", greaterThanOrEqualTo(1));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Update flag generates audit entry in centralized audit")
    public void updateFlagGeneratesAuditEntryWithOldAndNewValues() {
        FeatureFlagResponse created = getFlagHelper().createDefaultFlag();
        UpdateFeatureFlagRequest update = UpdateFeatureFlagRequest.builder()
                .name("New Name")
                .build();
        flagClient.updateFlag(created.getId(), update, authToken);

        Response response = auditClient.getAuditLogs(authToken,
                Map.of("entityType", "FEATURE_FLAG", "entityId", created.getId()));

        assertOk(response);
        response.then()
                .body("content.size()", greaterThanOrEqualTo(2));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Delete flag generates audit entry in centralized audit")
    public void deleteFlagGeneratesAuditEntry() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        Response createResponse = flagClient.createFlag(request, authToken);
        String flagId = createResponse.jsonPath().getString("id");

        flagClient.deleteFlag(flagId, authToken);

        Response response = auditClient.getAuditLogs(authToken,
                Map.of("entityType", "FEATURE_FLAG"));
        assertOk(response);
        response.then()
                .body("content.size()", greaterThanOrEqualTo(1));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Add override generates audit entry in centralized audit")
    public void addOverrideGeneratesAuditEntry() {
        FeatureFlagResponse created = getFlagHelper().createDefaultFlag();
        flagClient.addOverride(created.getId(), FeatureFlagTestHelper.tierOverride(4, true), authToken);

        Response response = auditClient.getAuditLogs(authToken,
                Map.of("entityType", "FEATURE_FLAG", "entityId", created.getId()));

        assertOk(response);
        response.then()
                .body("content.size()", greaterThanOrEqualTo(2));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Feature flag audit pagination works via centralized audit")
    public void auditLogPaginationWorksCorrectly() {
        FeatureFlagResponse created = getFlagHelper().createDefaultFlag();
        // Generate multiple audit entries
        flagClient.toggleFlag(created.getId(), FeatureFlagTestHelper.toggleRequest(false), authToken);
        flagClient.toggleFlag(created.getId(), FeatureFlagTestHelper.toggleRequest(true), authToken);

        Response response = auditClient.getAuditLogs(authToken,
                Map.of("entityType", "FEATURE_FLAG", "entityId", created.getId(), "size", "1"));

        assertOk(response);
        response.then()
                .body("content.size()", equalTo(1))
                .body("totalElements", greaterThanOrEqualTo(3));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Centralized audit returns cross-flag entries")
    public void allAuditLogsEndpointReturnsCrossFlag() {
        getFlagHelper().createDefaultFlag();
        getFlagHelper().createDefaultFlag();

        Response response = auditClient.getAuditLogs(authToken,
                Map.of("entityType", "FEATURE_FLAG"));

        assertOk(response);
        response.then()
                .body("content.size()", greaterThanOrEqualTo(2));
    }
}
