package ua.kpi.sc.test.api.tests.user;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.user.AssignPartnerLevelRequest;

import static org.hamcrest.Matchers.equalTo;

@Epic("User Management")
@Feature("Remove Partner Level")
public class UserRemovePartnerTest extends BaseUserTest {

    @Test(groups = {TestGroup.SMOKE},
            description = "Remove partner level returns 204")
    public void removePartnerLevelReturns204() {
        String userId = registerTestUser();
        AssignPartnerLevelRequest request = TestDataFactory.validAssignPartnerRequest();
        userClient.assignPartnerLevel(userId, request, authToken);

        Response response = userClient.removePartnerLevel(userId, request.getPartnerId(), authToken);

        assertNoContent(response);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Removed partner is gone from user response")
    public void removedPartnerGoneFromUser() {
        String userId = registerTestUser();
        AssignPartnerLevelRequest request = TestDataFactory.validAssignPartnerRequest();
        userClient.assignPartnerLevel(userId, request, authToken);
        userClient.removePartnerLevel(userId, request.getPartnerId(), authToken);

        Response response = userClient.getUserById(userId, authToken);

        assertOk(response);
        response.then()
                .body("partnerRoles.size()", equalTo(0));
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Remove partner for non-existent user returns 404")
    public void removePartnerForNonExistentUserReturns404() {
        Response response = userClient.removePartnerLevel(
                TestDataFactory.randomId(), TestDataFactory.randomId(), authToken);

        assertStatus(response, 404);
    }
}
