package ua.kpi.sc.test.api.data;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.kpi.sc.test.api.client.auth.AuthClient;
import ua.kpi.sc.test.api.model.auth.RegisterRequest;

public final class TestDataManager {

    private static final Logger log = LoggerFactory.getLogger(TestDataManager.class);
    private static final AuthClient authClient = new AuthClient();

    private TestDataManager() {}

    public static Response registerUserAndTrack(RegisterRequest request) {
        Response response = authClient.register(request);

        if (response.getStatusCode() == 201 || response.getStatusCode() == 200) {
            String userId = response.jsonPath().getString("id");
            if (userId != null) {
                CleanupRegistry.register(
                        "Delete user: " + request.getEmail(),
                        () -> log.info("Would delete user {} (cleanup not yet implemented)", userId)
                );
            }
        }

        return response;
    }

    public static RegisterRequest createAndRegisterUser() {
        RegisterRequest request = TestDataFactory.validRegisterRequest();
        registerUserAndTrack(request);
        return request;
    }
}
