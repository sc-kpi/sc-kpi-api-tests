package ua.kpi.sc.test.api.tests.user;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.TestGroup;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Epic("User Management")
@Feature("User List API")
public class UserTest extends BaseUserTest {

    @Test(groups = {TestGroup.SMOKE},
            description = "List users returns paginated response")
    public void listUsersReturnsPaginatedResponse() {
        Response response = userClient.getUsers(authToken);

        assertOk(response);
        response.then()
                .body("content", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(1));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "List users contains registered user")
    public void listUsersContainsRegisteredUser() {
        var registered = registerTestUserFull();

        Response response = userClient.getUsers(authToken, java.util.Map.of("search", registered.getEmail()));

        assertOk(response);
        response.then()
                .body("content.email", hasItem(registered.getEmail()));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "List users pagination works")
    public void listUsersPaginationWorks() {
        registerTestUser();

        Response response = userClient.getUsers(authToken,
                java.util.Map.of("size", "1"));

        assertOk(response);
        response.then()
                .body("content.size()", is(1));
    }
}
