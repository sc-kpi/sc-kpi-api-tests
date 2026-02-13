package ua.kpi.sc.test.api.tests.user;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;

import static org.hamcrest.Matchers.notNullValue;

@Epic("User Management")
@Feature("Get User By ID")
public class UserGetByIdTest extends BaseUserTest {

    @Test(groups = {TestGroup.SMOKE},
            description = "Get user by ID returns 200 with all fields")
    public void getUserByIdReturns200() {
        String userId = registerTestUser();

        Response response = userClient.getUserById(userId, authToken);

        assertOk(response);
        response.then()
                .body("id", notNullValue())
                .body("email", notNullValue())
                .body("firstName", notNullValue())
                .body("lastName", notNullValue())
                .body("capabilityTier", notNullValue())
                .body("active", notNullValue())
                .body("partnerRoles", notNullValue());
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Get user by ID returns partner roles array")
    public void getUserByIdReturnsPartnerRoles() {
        String userId = registerTestUser();

        // Assign a partner level
        var assignRequest = TestDataFactory.validAssignPartnerRequest();
        userClient.assignPartnerLevel(userId, assignRequest, authToken);

        Response response = userClient.getUserById(userId, authToken);

        assertOk(response);
        response.then()
                .body("partnerRoles.size()", org.hamcrest.Matchers.greaterThanOrEqualTo(1));
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Get non-existent user returns 404")
    public void getNonExistentUserReturns404() {
        Response response = userClient.getUserById(TestDataFactory.randomId(), authToken);

        assertStatus(response, 404);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Get user without auth returns 401")
    public void getUserWithoutAuthReturns401() {
        String userId = registerTestUser();

        Response response = userClient.getUserById(userId, null);

        assertStatus(response, 401);
    }
}
