package ua.kpi.sc.test.api.tests.user;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.user.UpdateStatusRequest;
import ua.kpi.sc.test.api.model.user.UserResponse;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("User Management")
@Feature("Update User Status")
public class UserUpdateStatusTest extends BaseUserTest {

    // ==================== SMOKE ====================

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "PATCH /users/{id}/status to deactivate returns 200 with active=false")
    public void deactivateUserReturns200() {
        String userId = registerTestUser();
        UpdateStatusRequest request = TestDataFactory.validUpdateStatusRequest(false);

        Response response = userClient.updateStatus(userId, request, authToken);

        assertOk(response);
        UserResponse body = response.as(UserResponse.class);
        assertThat(body.isActive()).isFalse();
    }

    // ==================== POSITIVE ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "PATCH /users/{id}/status to reactivate returns 200 with active=true")
    public void reactivateUserReturns200() {
        String userId = registerTestUser();

        // First deactivate
        userClient.updateStatus(userId, TestDataFactory.validUpdateStatusRequest(false), authToken);

        // Then reactivate
        UpdateStatusRequest request = TestDataFactory.validUpdateStatusRequest(true);
        Response response = userClient.updateStatus(userId, request, authToken);

        assertOk(response);
        UserResponse body = response.as(UserResponse.class);
        assertThat(body.isActive()).isTrue();
    }

    // ==================== NEGATIVE ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "PATCH /users/{id}/status for non-existent user returns 404")
    public void updateStatusForNonExistentUserReturns404() {
        UpdateStatusRequest request = TestDataFactory.validUpdateStatusRequest(false);

        Response response = userClient.updateStatus(TestDataFactory.randomId(), request, authToken);

        assertStatus(response, 404);
    }

    // ==================== METHOD NOT ALLOWED ====================

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PUT /users/{id}/status returns 405 Method Not Allowed")
    public void putStatusReturns405() {
        String userId = registerTestUser();
        UpdateStatusRequest request = TestDataFactory.validUpdateStatusRequest(false);

        Response response = userClient.updateStatusPut(userId, request, authToken);

        assertStatus(response, 405);
    }
}
