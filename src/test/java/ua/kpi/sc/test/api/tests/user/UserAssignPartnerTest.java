package ua.kpi.sc.test.api.tests.user;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.user.AssignPartnerLevelRequest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@Epic("User Management")
@Feature("Assign Partner Level")
public class UserAssignPartnerTest extends BaseUserTest {

    @Test(groups = {TestGroup.SMOKE},
            description = "Assign partner level returns 201")
    public void assignPartnerLevelReturns201() {
        String userId = registerTestUser();
        AssignPartnerLevelRequest request = TestDataFactory.validAssignPartnerRequest();

        Response response = userClient.assignPartnerLevel(userId, request, authToken);

        assertCreated(response);
        response.then()
                .body("partnerId", equalTo(request.getPartnerId()))
                .body("level", equalTo("basic"))
                .body("assignedAt", notNullValue());
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Assign partner with each level succeeds")
    public void assignPartnerEachLevel() {
        for (String level : new String[]{"basic", "documents", "full"}) {
            String userId = registerTestUser();
            AssignPartnerLevelRequest request = TestDataFactory.assignPartnerRequest(level);

            Response response = userClient.assignPartnerLevel(userId, request, authToken);

            assertCreated(response);
            response.then()
                    .body("level", equalTo(level));
        }
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Assign partner level upserts existing")
    public void assignPartnerUpserts() {
        String userId = registerTestUser();
        AssignPartnerLevelRequest request = TestDataFactory.assignPartnerRequest("basic");

        // First assignment
        userClient.assignPartnerLevel(userId, request, authToken);

        // Upsert to full with same partnerId
        request.setLevel("full");
        Response response = userClient.assignPartnerLevel(userId, request, authToken);

        assertCreated(response);
        response.then()
                .body("level", equalTo("full"));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Assigned partner appears in user response")
    public void assignedPartnerAppearsInUserResponse() {
        String userId = registerTestUser();
        AssignPartnerLevelRequest request = TestDataFactory.validAssignPartnerRequest();
        userClient.assignPartnerLevel(userId, request, authToken);

        Response response = userClient.getUserById(userId, authToken);

        assertOk(response);
        response.then()
                .body("partnerRoles.size()", greaterThanOrEqualTo(1));
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Assign invalid level returns 400")
    public void assignInvalidLevelReturns400() {
        String userId = registerTestUser();
        AssignPartnerLevelRequest request = AssignPartnerLevelRequest.builder()
                .partnerId(TestDataFactory.randomId())
                .level("invalid")
                .build();

        Response response = userClient.assignPartnerLevel(userId, request, authToken);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Assign to non-existent user returns 404")
    public void assignToNonExistentUserReturns404() {
        AssignPartnerLevelRequest request = TestDataFactory.validAssignPartnerRequest();

        Response response = userClient.assignPartnerLevel(TestDataFactory.randomId(), request, authToken);

        assertStatus(response, 404);
    }
}
