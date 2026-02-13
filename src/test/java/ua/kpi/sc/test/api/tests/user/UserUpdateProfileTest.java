package ua.kpi.sc.test.api.tests.user;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.user.UserResponse;
import ua.kpi.sc.test.api.model.user.UserUpdateRequest;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("User Management")
@Feature("Update User Profile")
public class UserUpdateProfileTest extends BaseUserTest {

    // ==================== SMOKE ====================

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE},
            description = "PATCH /users/{id} with valid data returns 200 with updated fields")
    public void updateUserWithValidData() {
        String userId = registerTestUser();
        UserUpdateRequest request = TestDataFactory.validUserUpdateRequest();

        Response response = userClient.updateUser(userId, request, authToken);

        assertOk(response);
        UserResponse body = response.as(UserResponse.class);
        assertThat(body.getFirstName()).isEqualTo(request.getFirstName());
        assertThat(body.getLastName()).isEqualTo(request.getLastName());
    }

    // ==================== POSITIVE ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "PATCH /users/{id} preserves email, tier, and active status")
    public void updateUserPreservesOtherFields() {
        UserResponse original = registerTestUserFull();
        UserUpdateRequest request = TestDataFactory.validUserUpdateRequest();

        Response response = userClient.updateUser(original.getId(), request, authToken);

        assertOk(response);
        UserResponse updated = response.as(UserResponse.class);
        assertThat(updated.getEmail()).isEqualTo(original.getEmail());
        assertThat(updated.getCapabilityTier()).isEqualTo(original.getCapabilityTier());
        assertThat(updated.isActive()).isEqualTo(original.isActive());
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "PATCH /users/{id} with only firstName still updates successfully")
    public void updateOnlyFirstName() {
        String userId = registerTestUser();
        UserUpdateRequest request = UserUpdateRequest.builder()
                .firstName("OnlyFirst")
                .lastName("Kept")
                .build();

        Response response = userClient.updateUser(userId, request, authToken);

        assertOk(response);
        UserResponse body = response.as(UserResponse.class);
        assertThat(body.getFirstName()).isEqualTo("OnlyFirst");
    }

    // ==================== NEGATIVE ====================

    @Test(groups = {TestGroup.NEGATIVE},
            description = "PATCH /users/{id} for non-existent user returns 404")
    public void updateNonExistentUserReturns404() {
        UserUpdateRequest request = TestDataFactory.validUserUpdateRequest();

        Response response = userClient.updateUser(TestDataFactory.randomId(), request, authToken);

        assertStatus(response, 404);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "PATCH /users/{id} with empty body returns 400")
    public void updateWithEmptyBodyReturns400() {
        String userId = registerTestUser();
        UserUpdateRequest request = UserUpdateRequest.builder()
                .firstName("")
                .lastName("")
                .build();

        Response response = userClient.updateUser(userId, request, authToken);

        assertStatus(response, 400);
    }

    // ==================== METHOD NOT ALLOWED ====================

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PUT /users/{id} returns 405 Method Not Allowed")
    public void putUserReturns405() {
        String userId = registerTestUser();
        UserUpdateRequest request = TestDataFactory.validUserUpdateRequest();

        Response response = userClient.updateUserPut(userId, request, authToken);

        assertStatus(response, 405);
    }
}
