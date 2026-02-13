package ua.kpi.sc.test.api.tests.user;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.user.CreateUserRequest;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

@Epic("User Management")
@Feature("Create User")
public class UserCreateTest extends BaseUserTest {

    @Test(groups = {TestGroup.SMOKE},
            description = "Create user returns 201 with correct fields")
    public void createUserReturns201() {
        CreateUserRequest request = TestDataFactory.validCreateUserRequest();

        Response response = userClient.createUser(request, authToken);

        assertCreated(response);
        response.then()
                .body("id", notNullValue())
                .body("email", equalTo(request.getEmail()))
                .body("active", equalTo(true));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Create user with each tier level succeeds")
    public void createUserWithEachTier() {
        for (int tier = 0; tier <= 5; tier++) {
            CreateUserRequest request = TestDataFactory.createUserRequestWithTier(tier);
            Response response = userClient.createUser(request, authToken);
            assertCreated(response);
            response.then()
                    .body("capabilityTier", equalTo(tier));
        }
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Created user appears in user list")
    public void createdUserAppearsInList() {
        CreateUserRequest request = TestDataFactory.validCreateUserRequest();
        Response createResponse = userClient.createUser(request, authToken);
        assertCreated(createResponse);

        Response listResponse = userClient.getUsers(authToken, Map.of("search", request.getEmail()));
        assertOk(listResponse);
        listResponse.then()
                .body("content.email", hasItem(request.getEmail()));
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Create user with duplicate email returns 409")
    public void createUserDuplicateEmailReturns409() {
        CreateUserRequest request = TestDataFactory.validCreateUserRequest();
        userClient.createUser(request, authToken);

        // Try to create again with same email
        Response response = userClient.createUser(request, authToken);
        assertStatus(response, 409);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Create user with invalid email returns 400")
    public void createUserInvalidEmailReturns400() {
        CreateUserRequest request = TestDataFactory.validCreateUserRequest();
        request.setEmail("not-an-email");

        Response response = userClient.createUser(request, authToken);
        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Create user with short password returns 400")
    public void createUserShortPasswordReturns400() {
        CreateUserRequest request = TestDataFactory.validCreateUserRequest();
        request.setPassword("short");

        Response response = userClient.createUser(request, authToken);
        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Create user with blank names returns 400")
    public void createUserBlankNamesReturns400() {
        CreateUserRequest request = TestDataFactory.validCreateUserRequest();
        request.setFirstName("");
        request.setLastName("");

        Response response = userClient.createUser(request, authToken);
        assertStatus(response, 400);
    }

    @Test(groups = {TestGroup.SECURITY},
            description = "Create user without auth returns 401")
    public void createUserWithoutAuthReturns401() {
        CreateUserRequest request = TestDataFactory.validCreateUserRequest();

        Response response = userClient.createUser(request, null);
        assertStatus(response, 401);
    }
}
