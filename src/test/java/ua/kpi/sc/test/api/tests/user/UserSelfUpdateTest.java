package ua.kpi.sc.test.api.tests.user;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.base.BaseAuthenticatedApiTest;
import ua.kpi.sc.test.api.client.user.UserClient;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.user.UserUpdateRequest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Epic("User Management")
@Feature("Self Profile Update")
public class UserSelfUpdateTest extends BaseAuthenticatedApiTest {

    protected final UserClient userClient = new UserClient();

    @Test(groups = {TestGroup.SMOKE},
            description = "Update self profile returns 200")
    public void updateSelfReturns200() {
        UserUpdateRequest request = TestDataFactory.validUserUpdateRequest();

        Response response = userClient.updateSelf(request, authToken);

        assertOk(response);
        response.then()
                .body("firstName", equalTo(request.getFirstName()))
                .body("lastName", equalTo(request.getLastName()));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Update self preserves other fields")
    public void updateSelfPreservesOtherFields() {
        // Get current state
        Response currentResponse = userClient.getCurrentUser(authToken);
        assertOk(currentResponse);
        String email = currentResponse.jsonPath().getString("email");
        int tier = currentResponse.jsonPath().getInt("capabilityTier");
        boolean active = currentResponse.jsonPath().getBoolean("active");

        // Update name only
        UserUpdateRequest request = TestDataFactory.validUserUpdateRequest();
        Response response = userClient.updateSelf(request, authToken);

        assertOk(response);
        response.then()
                .body("email", equalTo(email))
                .body("capabilityTier", equalTo(tier))
                .body("active", equalTo(active));
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Update self with blank name returns 400")
    public void updateSelfBlankNameReturns400() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .firstName("")
                .lastName("")
                .build();

        Response response = userClient.updateSelf(request, authToken);

        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Update self without auth returns 401")
    public void updateSelfWithoutAuthReturns401() {
        UserUpdateRequest request = TestDataFactory.validUserUpdateRequest();

        Response response = userClient.updateSelf(request, null);

        assertStatus(response, 401);
    }

    @Test(groups = {TestGroup.METHOD_NOT_ALLOWED},
            description = "PUT /users/me returns 405")
    public void putMeReturns405() {
        UserUpdateRequest request = TestDataFactory.validUserUpdateRequest();

        Response response = userClient.updateSelfPut(request, authToken);

        assertStatus(response, 405);
    }

    @Test(groups = {TestGroup.SMOKE},
            description = "GET /users/me returns current user profile")
    public void getMeReturns200() {
        Response response = userClient.getCurrentUser(authToken);

        assertOk(response);
        response.then()
                .body("id", notNullValue())
                .body("email", notNullValue())
                .body("firstName", notNullValue())
                .body("lastName", notNullValue());
    }
}
