package ua.kpi.sc.test.api.tests.user;

import io.restassured.response.Response;
import ua.kpi.sc.test.api.base.BaseAdminApiTest;
import ua.kpi.sc.test.api.client.auth.AuthClient;
import ua.kpi.sc.test.api.client.user.UserClient;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.auth.RegisterRequest;
import ua.kpi.sc.test.api.model.user.UserResponse;

public abstract class BaseUserTest extends BaseAdminApiTest {

    protected final AuthClient authClient = new AuthClient();
    protected final UserClient userClient = new UserClient();

    protected String registerTestUser() {
        RegisterRequest request = TestDataFactory.validRegisterRequest();
        Response response = authClient.register(request);
        assertCreated(response);
        return response.jsonPath().getString("id");
    }

    protected UserResponse registerTestUserFull() {
        String userId = registerTestUser();
        Response response = userClient.getUserById(userId, authToken);
        assertOk(response);
        return response.as(UserResponse.class);
    }
}
