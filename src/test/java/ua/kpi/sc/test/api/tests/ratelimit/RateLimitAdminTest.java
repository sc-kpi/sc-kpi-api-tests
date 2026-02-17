package ua.kpi.sc.test.api.tests.ratelimit;

import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.annotation.Authentication;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.RateLimitTestHelper;
import ua.kpi.sc.test.api.model.ratelimit.CreateRateLimitRuleRequest;
import ua.kpi.sc.test.api.model.ratelimit.RateLimitRuleResponse;
import ua.kpi.sc.test.api.model.ratelimit.UpdateRateLimitRuleRequest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@Feature("Rate Limit Admin")
public class RateLimitAdminTest extends BaseRateLimitTest {

    // ==================== CRUD ====================

    @Test(groups = {TestGroup.SMOKE}, description = "Admin can list rate limit rules")
    public void listRules_returns200() {
        Response response = rateLimitClient.listRules(authToken);
        assertOk(response);
        response.then()
                .body("content", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(0));
    }

    @Test(groups = {TestGroup.POSITIVE}, description = "Admin can create a rate limit rule")
    public void createRule_returns201() {
        CreateRateLimitRuleRequest request = RateLimitTestHelper.validCreateRequest();

        Response response = rateLimitClient.createRule(request, authToken);

        assertCreated(response);
        response.then()
                .body("name", equalTo(request.getName()))
                .body("endpointPattern", equalTo(request.getEndpointPattern()))
                .body("scope", equalTo("IP"))
                .body("limitPerPeriod", equalTo(60))
                .body("periodSeconds", equalTo(60))
                .body("burstCapacity", equalTo(90))
                .body("enabled", equalTo(true))
                .body("id", notNullValue());

        // Track for cleanup
        String id = response.jsonPath().getString("id");
        getRateLimitHelper();
        rateLimitHelper = new RateLimitTestHelper(rateLimitClient, authToken);
        rateLimitClient.deleteRule(id, authToken);
    }

    @Test(groups = {TestGroup.POSITIVE}, description = "Admin can get a rule by ID")
    public void getRule_returns200() {
        RateLimitRuleResponse created = getRateLimitHelper().createDefaultRule();

        Response response = rateLimitClient.getRule(created.getId(), authToken);

        assertOk(response);
        response.then()
                .body("id", equalTo(created.getId()))
                .body("name", equalTo(created.getName()));
    }

    @Test(groups = {TestGroup.POSITIVE}, description = "Admin can update a rule")
    public void updateRule_returns200() {
        RateLimitRuleResponse created = getRateLimitHelper().createDefaultRule();
        UpdateRateLimitRuleRequest update = UpdateRateLimitRuleRequest.builder()
                .description("Updated description")
                .limitPerPeriod(120)
                .build();

        Response response = rateLimitClient.updateRule(created.getId(), update, authToken);

        assertOk(response);
        response.then()
                .body("description", equalTo("Updated description"))
                .body("limitPerPeriod", equalTo(120));
    }

    @Test(groups = {TestGroup.POSITIVE}, description = "Admin can toggle a rule")
    public void toggleRule_changesEnabledState() {
        RateLimitRuleResponse created = getRateLimitHelper().createDefaultRule();

        Response response = rateLimitClient.toggleRule(
                created.getId(),
                RateLimitTestHelper.toggleRequest(false),
                authToken);

        assertOk(response);
        response.then().body("enabled", equalTo(false));
    }

    @Test(groups = {TestGroup.POSITIVE}, description = "Admin can delete a rule")
    public void deleteRule_returns204() {
        RateLimitRuleResponse created = getRateLimitHelper().createDefaultRule();

        Response response = rateLimitClient.deleteRule(created.getId(), authToken);

        assertNoContent(response);

        // Verify it's gone
        Response getResponse = rateLimitClient.getRule(created.getId(), authToken);
        assertStatus(getResponse, 404);
    }

    @Test(groups = {TestGroup.POSITIVE}, description = "Admin can get stats")
    public void getStats_returns200() {
        Response response = rateLimitClient.getStats(authToken);

        assertOk(response);
        response.then()
                .body("totalViolations", greaterThanOrEqualTo(0))
                .body("activeBuckets", greaterThanOrEqualTo(0))
                .body("violationsByEndpoint", notNullValue());
    }

    // ==================== Validation ====================

    @Test(groups = {TestGroup.NEGATIVE}, description = "Duplicate rule name returns 409")
    public void createRule_duplicateName_returns409() {
        RateLimitRuleResponse created = getRateLimitHelper().createDefaultRule();
        CreateRateLimitRuleRequest duplicate = RateLimitTestHelper.createRequestWithName(created.getName());

        Response response = rateLimitClient.createRule(duplicate, authToken);

        assertStatus(response, 409);
    }

    @Test(groups = {TestGroup.NEGATIVE}, description = "Non-existent rule returns 404")
    public void getRule_nonExistent_returns404() {
        Response response = rateLimitClient.getRule("00000000-0000-0000-0000-000000000000", authToken);

        assertStatus(response, 404);
    }

    @Test(groups = {TestGroup.VALIDATION}, description = "Create rule with blank name returns 400")
    public void createRule_blankName_returns400() {
        CreateRateLimitRuleRequest request = RateLimitTestHelper.validCreateRequest();
        request.setName("");

        Response response = rateLimitClient.createRule(request, authToken);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.VALIDATION}, description = "Create rule with zero limit returns 400")
    public void createRule_zeroLimit_returns400() {
        CreateRateLimitRuleRequest request = RateLimitTestHelper.validCreateRequest();
        request.setLimitPerPeriod(0);

        Response response = rateLimitClient.createRule(request, authToken);

        assertStatus(response, 400);
    }

    // ==================== Schema Validation ====================

    @Test(groups = {TestGroup.SCHEMA}, description = "Create rule response has all expected fields")
    public void createRule_responseHasExpectedFields() {
        RateLimitRuleResponse created = getRateLimitHelper().createDefaultRule();

        Response response = rateLimitClient.getRule(created.getId(), authToken);

        assertOk(response);
        response.then()
                .body("id", notNullValue())
                .body("name", notNullValue())
                .body("endpointPattern", notNullValue())
                .body("limitPerPeriod", notNullValue())
                .body("periodSeconds", notNullValue())
                .body("burstCapacity", notNullValue())
                .body("scope", notNullValue())
                .body("priority", notNullValue())
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());
    }

    // ==================== Security ====================

    @Test(groups = {TestGroup.SECURITY}, description = "Unauthenticated request returns 401")
    public void listRules_noAuth_returns401() {
        Response response = rateLimitClient.listRules(null);
        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.SECURITY}, description = "Basic user cannot create rules")
    @Authentication(tier = "BASIC")
    public void createRule_basicUser_returns403() {
        CreateRateLimitRuleRequest request = RateLimitTestHelper.validCreateRequest();

        Response response = rateLimitClient.createRule(request, authToken);

        assertStatus(response, 403);
    }

    @Test(groups = {TestGroup.SECURITY}, description = "Basic user cannot delete rules")
    @Authentication(tier = "BASIC")
    public void deleteRule_basicUser_returns403() {
        Response response = rateLimitClient.deleteRule("00000000-0000-0000-0000-000000000000", authToken);

        assertStatus(response, 403);
    }

    @Test(groups = {TestGroup.SECURITY}, description = "Basic user cannot toggle rules")
    @Authentication(tier = "BASIC")
    public void toggleRule_basicUser_returns403() {
        Response response = rateLimitClient.toggleRule(
                "00000000-0000-0000-0000-000000000000",
                RateLimitTestHelper.toggleRequest(false),
                authToken);

        assertStatus(response, 403);
    }

    // ==================== Scope Variants ====================

    @Test(groups = {TestGroup.POSITIVE}, description = "Admin can create a global scope rule")
    public void createRule_globalScope_returns201() {
        CreateRateLimitRuleRequest request = RateLimitTestHelper.globalScopeRequest();

        RateLimitRuleResponse created = getRateLimitHelper().createRule(request);

        Response response = rateLimitClient.getRule(created.getId(), authToken);
        assertOk(response);
        response.then().body("scope", equalTo("GLOBAL"));
    }

    @Test(groups = {TestGroup.POSITIVE}, description = "Admin can create a tier scope rule with target")
    public void createRule_tierScope_returns201() {
        CreateRateLimitRuleRequest request = RateLimitTestHelper.tierScopeRequest(1);

        RateLimitRuleResponse created = getRateLimitHelper().createRule(request);

        Response response = rateLimitClient.getRule(created.getId(), authToken);
        assertOk(response);
        response.then()
                .body("scope", equalTo("TIER"))
                .body("targetTier", equalTo(1));
    }

    // ==================== Pagination ====================

    @Test(groups = {TestGroup.POSITIVE}, description = "List rules respects page size")
    public void listRules_pagination_respectsPageSize() {
        Response response = rateLimitClient.listRules(authToken, java.util.Map.of("page", "0", "size", "5"));

        assertOk(response);
        response.then().body("size", equalTo(5));
    }
}
