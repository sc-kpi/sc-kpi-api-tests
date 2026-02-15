package ua.kpi.sc.test.api.tests.audit;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.annotation.Authentication;
import ua.kpi.sc.test.api.base.BaseAdminApiTest;
import ua.kpi.sc.test.api.client.audit.AuditClient;
import ua.kpi.sc.test.api.config.TestGroup;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@Epic("Audit")
@Feature("Audit Log API")
public class AuditTest extends BaseAdminApiTest {

    private final AuditClient auditClient = new AuditClient();

    // ==================== Smoke ====================

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "Get audit logs returns 200 with paginated response")
    public void getAuditLogs_returns200() {
        Response response = auditClient.getAuditLogs(authToken);

        assertOk(response);
        response.then()
                .body("content", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(0));
    }

    @Test(groups = {TestGroup.REGRESSION, TestGroup.SECURITY},
            description = "Non-admin access to audit logs returns 403")
    @Authentication(tier = "BASIC")
    public void nonAdminAccess_returns403() {
        Response response = auditClient.getAuditLogs(authToken);

        assertStatus(response, 403);
    }

    // ==================== Filtering ====================

    @Test(groups = {TestGroup.REGRESSION, TestGroup.POSITIVE},
            description = "Filter audit logs by entity type returns filtered results")
    public void filterByEntityType_returnsFilteredResults() {
        Response response = auditClient.getAuditLogs(authToken,
                Map.of("entityType", "FEATURE_FLAG"));

        assertOk(response);
        response.then()
                .body("content", notNullValue());
    }

    @Test(groups = {TestGroup.REGRESSION, TestGroup.POSITIVE},
            description = "Filter audit logs by action returns filtered results")
    public void filterByAction_returnsFilteredResults() {
        Response response = auditClient.getAuditLogs(authToken,
                Map.of("action", "CREATED"));

        assertOk(response);
        response.then()
                .body("content", notNullValue());
    }

    @Test(groups = {TestGroup.REGRESSION, TestGroup.POSITIVE},
            description = "Filter audit logs by source module returns filtered results")
    public void filterBySourceModule_returnsFilteredResults() {
        Response response = auditClient.getAuditLogs(authToken,
                Map.of("sourceModule", "feature-flag"));

        assertOk(response);
        response.then()
                .body("content", notNullValue());
    }

    @Test(groups = {TestGroup.REGRESSION, TestGroup.POSITIVE},
            description = "Filter audit logs by date range returns filtered results")
    public void filterByDateRange_returnsFilteredResults() {
        String from = Instant.now().minus(30, ChronoUnit.DAYS).toString();
        String to = Instant.now().toString();

        Response response = auditClient.getAuditLogs(authToken,
                Map.of("from", from, "to", to));

        assertOk(response);
        response.then()
                .body("content", notNullValue());
    }

    @Test(groups = {TestGroup.REGRESSION, TestGroup.POSITIVE},
            description = "Search audit logs by text returns matches")
    public void searchByText_returnsMatches() {
        Response response = auditClient.getAuditLogs(authToken,
                Map.of("search", "admin"));

        assertOk(response);
        response.then()
                .body("content", notNullValue());
    }

    // ==================== Pagination ====================

    @Test(groups = {TestGroup.REGRESSION, TestGroup.POSITIVE},
            description = "Audit logs pagination works correctly")
    public void pagination_worksCorrectly() {
        Response response = auditClient.getAuditLogs(authToken,
                Map.of("page", "0", "size", "5"));

        assertOk(response);
        response.then()
                .body("content", notNullValue())
                .body("size", equalTo(5))
                .body("totalElements", greaterThanOrEqualTo(0));
    }

    @Test(groups = {TestGroup.REGRESSION, TestGroup.POSITIVE},
            description = "Audit logs empty page returns empty content")
    public void pagination_emptyPage_returnsEmptyContent() {
        Response response = auditClient.getAuditLogs(authToken,
                Map.of("page", "9999"));

        assertOk(response);
        response.then()
                .body("content.size()", equalTo(0));
    }

    // ==================== Export ====================

    @Test(groups = {TestGroup.REGRESSION, TestGroup.POSITIVE},
            description = "Export audit logs as CSV returns 200 with CSV content type")
    public void exportCsv_returns200_withCsvContentType() {
        Response response = auditClient.exportCsv(authToken);

        assertOk(response);
        response.then()
                .contentType(containsString("text/csv"));

        String body = response.getBody().asString();
        assertThat(body, containsString("Timestamp,Actor,Action,Entity Type"));
    }

    // ==================== Stats ====================

    @Test(groups = {TestGroup.REGRESSION, TestGroup.POSITIVE},
            description = "Get audit stats returns 200 with expected shape")
    public void getStats_returns200_withExpectedShape() {
        Response response = auditClient.getStats(authToken);

        assertOk(response);
        response.then()
                .body("totalEvents", greaterThanOrEqualTo(0))
                .body("eventsLast24h", greaterThanOrEqualTo(0))
                .body("byEntityType", notNullValue())
                .body("byAction", notNullValue());
    }

    // ==================== Security ====================

    @Test(groups = {TestGroup.REGRESSION, TestGroup.SECURITY},
            description = "Basic user access to audit logs returns 403")
    @Authentication(tier = "BASIC")
    public void basicUserAccess_returns403() {
        Response response = auditClient.getAuditLogs(authToken);

        assertStatus(response, 403);
    }

    @Test(groups = {TestGroup.REGRESSION, TestGroup.SECURITY},
            description = "Unauthenticated access to audit logs returns 401")
    public void unauthenticatedAccess_returns401() {
        Response response = auditClient.getAuditLogs(null);

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.REGRESSION, TestGroup.SECURITY},
            description = "Basic user cannot export audit CSV — returns 403")
    @Authentication(tier = "BASIC")
    public void basicUserCannotExportCsv_returns403() {
        Response response = auditClient.exportCsv(authToken);

        assertStatus(response, 403);
    }

    @Test(groups = {TestGroup.REGRESSION, TestGroup.SECURITY},
            description = "Basic user cannot access audit stats — returns 403")
    @Authentication(tier = "BASIC")
    public void basicUserCannotAccessStats_returns403() {
        Response response = auditClient.getStats(authToken);

        assertStatus(response, 403);
    }

    // ==================== Schema Validation ====================

    @Test(groups = {TestGroup.REGRESSION, TestGroup.SCHEMA},
            description = "Audit event response matches expected schema")
    public void responseMatchesAuditEventSchema() {
        Response response = auditClient.getAuditLogs(authToken);

        assertOk(response);
        response.then()
                .body("content", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(0))
                .body("totalPages", greaterThanOrEqualTo(0))
                .body("size", greaterThanOrEqualTo(1));

        // Verify first item has expected fields if content is non-empty
        int contentSize = response.jsonPath().getInt("content.size()");
        if (contentSize > 0) {
            response.then()
                    .body("content[0].id", notNullValue())
                    .body("content[0].action", notNullValue())
                    .body("content[0].entityType", notNullValue())
                    .body("content[0].sourceModule", notNullValue())
                    .body("content[0].createdAt", notNullValue());
        }
    }

    // AssertJ-style helper for CSV body assertions
    private static void assertThat(String actual, org.hamcrest.Matcher<String> matcher) {
        org.hamcrest.MatcherAssert.assertThat(actual, matcher);
    }
}
