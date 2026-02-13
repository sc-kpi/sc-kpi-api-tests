package ua.kpi.sc.test.api.tests.user;

import java.util.Map;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.user.CreateUserRequest;
import ua.kpi.sc.test.api.model.user.UpdateStatusRequest;
import ua.kpi.sc.test.api.model.user.UpdateTierRequest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;

@Epic("User Management")
@Feature("User Search and Filter")
public class UserSearchFilterTest extends BaseUserTest {

    @Test(groups = {TestGroup.SMOKE},
            description = "Search users by name returns results")
    public void searchUsersByNameReturnsResults() {
        String uniqueName = "UniqueXyz" + System.nanoTime();
        CreateUserRequest request = TestDataFactory.validCreateUserRequest();
        request.setFirstName(uniqueName);
        userClient.createUser(request, authToken);

        Response response = userClient.getUsers(authToken, Map.of("search", uniqueName));

        assertOk(response);
        response.then()
                .body("content.firstName", hasItem(uniqueName));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Search by email substring returns results")
    public void searchByEmailSubstring() {
        CreateUserRequest request = TestDataFactory.validCreateUserRequest();
        userClient.createUser(request, authToken);
        String emailDomain = request.getEmail().split("@")[1];

        Response response = userClient.getUsers(authToken, Map.of("search", emailDomain));

        assertOk(response);
        response.then()
                .body("content.size()", greaterThanOrEqualTo(1));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Filter by tier returns matching users")
    public void filterByTierReturnsMatching() {
        CreateUserRequest request = TestDataFactory.createUserRequestWithTier(3);
        Response createResponse = userClient.createUser(request, authToken);
        assertCreated(createResponse);

        Response response = userClient.getUsers(authToken, Map.of("tier", "3"));

        assertOk(response);
        response.then()
                .body("content.size()", greaterThanOrEqualTo(1))
                .body("content[0].capabilityTier", equalTo(3));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Filter by active status returns matching")
    public void filterByActiveStatus() {
        String userId = registerTestUser();
        userClient.updateStatus(userId,
                UpdateStatusRequest.builder().active(false).build(), authToken);

        Response response = userClient.getUsers(authToken, Map.of("active", "false"));

        assertOk(response);
        response.then()
                .body("content.size()", greaterThanOrEqualTo(1));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Combined search and filter returns results")
    public void combinedSearchAndFilter() {
        String uniqueName = "CombinedFilter" + System.nanoTime();
        CreateUserRequest request = TestDataFactory.createUserRequestWithTier(1);
        request.setFirstName(uniqueName);
        userClient.createUser(request, authToken);

        Response response = userClient.getUsers(authToken,
                Map.of("search", uniqueName, "tier", "1", "active", "true"));

        assertOk(response);
        response.then()
                .body("content.size()", greaterThanOrEqualTo(1));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Search with no match returns empty content")
    public void searchNoMatchReturnsEmpty() {
        Response response = userClient.getUsers(authToken,
                Map.of("search", "NonExistentXyzQwerty99999"));

        assertOk(response);
        response.then()
                .body("content.size()", equalTo(0));
    }
}
