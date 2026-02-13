package ua.kpi.sc.test.api.tests.user;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.user.UpdateTierRequest;
import ua.kpi.sc.test.api.model.user.UserResponse;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("User Management")
@Feature("Update User Tier")
public class UserUpdateTierTest extends BaseUserTest {

    // ==================== SMOKE ====================

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "PATCH /users/{id}/tier with valid tier returns 200")
    public void updateTierWithValidValue() {
        String userId = registerTestUser();
        UpdateTierRequest request = UpdateTierRequest.builder().tier(3).build();

        Response response = userClient.updateTier(userId, request, authToken);

        assertOk(response);
        UserResponse body = response.as(UserResponse.class);
        assertThat(body.getCapabilityTier()).isEqualTo(3);
    }

    // ==================== POSITIVE ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "PATCH /users/{id}/tier accepts each tier level 0-5")
    public void updateTierToEachLevel() {
        String userId = registerTestUser();

        for (int tier = 0; tier <= 5; tier++) {
            UpdateTierRequest request = UpdateTierRequest.builder().tier(tier).build();

            Response response = userClient.updateTier(userId, request, authToken);

            assertOk(response);
            UserResponse body = response.as(UserResponse.class);
            assertThat(body.getCapabilityTier()).isEqualTo(tier);
        }
    }

    // ==================== NEGATIVE ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "PATCH /users/{id}/tier with invalid tier returns 400")
    public void updateTierWithInvalidValueReturns400() {
        String userId = registerTestUser();
        UpdateTierRequest request = UpdateTierRequest.builder().tier(-1).build();

        Response response = userClient.updateTier(userId, request, authToken);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "PATCH /users/{id}/tier for non-existent user returns 404")
    public void updateTierForNonExistentUserReturns404() {
        UpdateTierRequest request = UpdateTierRequest.builder().tier(3).build();

        Response response = userClient.updateTier(TestDataFactory.randomId(), request, authToken);

        assertStatus(response, 404);
    }

    // ==================== METHOD NOT ALLOWED ====================

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PUT /users/{id}/tier returns 405 Method Not Allowed")
    public void putTierReturns405() {
        String userId = registerTestUser();
        UpdateTierRequest request = UpdateTierRequest.builder().tier(3).build();

        Response response = userClient.updateTierPut(userId, request, authToken);

        assertStatus(response, 405);
    }
}
