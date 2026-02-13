package ua.kpi.sc.test.api.tests.user;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;

import static org.hamcrest.Matchers.equalTo;

@Epic("User Management")
@Feature("Delete User")
public class UserDeleteTest extends BaseUserTest {

    @Test(groups = {TestGroup.SMOKE},
            description = "Delete user returns 204")
    public void deleteUserReturns204() {
        String userId = registerTestUser();

        Response response = userClient.deleteUser(userId, authToken);

        assertNoContent(response);
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Deleted user is deactivated")
    public void deletedUserIsDeactivated() {
        String userId = registerTestUser();
        userClient.deleteUser(userId, authToken);

        Response response = userClient.getUserById(userId, authToken);

        assertOk(response);
        response.then()
                .body("active", equalTo(false));
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Delete non-existent user returns 404")
    public void deleteNonExistentUserReturns404() {
        Response response = userClient.deleteUser(TestDataFactory.randomId(), authToken);

        assertStatus(response, 404);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Delete user without auth returns 401")
    public void deleteUserWithoutAuthReturns401() {
        String userId = registerTestUser();

        Response response = userClient.deleteUser(userId, null);

        assertStatus(response, 401);
    }
}
