package ua.kpi.sc.test.api.tests.user;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.base.BaseAuthenticatedApiTest;
import ua.kpi.sc.test.api.config.TestGroup;

@Epic("User Management")
@Feature("User API")
public class UserTest extends BaseAuthenticatedApiTest {

    @Test(groups = {TestGroup.TODO},
            description = "Get current user profile returns 200")
    public void getCurrentUserProfile() {
        // TODO: Implement when /api/v1/users/me is ready
    }

    @Test(groups = {TestGroup.TODO},
            description = "Update user profile returns 200")
    public void updateUserProfile() {
        // TODO: Implement when /api/v1/users/{id} PUT is ready
    }

    @Test(groups = {TestGroup.TODO},
            description = "List users returns paginated response")
    public void listUsersReturnsPaginatedResponse() {
        // TODO: Implement when /api/v1/users GET is ready
    }

    @Test(groups = {TestGroup.TODO},
            description = "Get non-existent user returns 404")
    public void getNonExistentUserReturns404() {
        // TODO: Implement when /api/v1/users/{id} GET is ready
    }

    @Test(groups = {TestGroup.TODO},
            description = "Unauthenticated access returns 401")
    public void unauthenticatedAccessReturns401() {
        // TODO: Implement when auth is ready
    }
}
