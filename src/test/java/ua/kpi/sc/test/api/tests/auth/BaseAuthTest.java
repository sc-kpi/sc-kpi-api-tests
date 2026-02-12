package ua.kpi.sc.test.api.tests.auth;

import io.restassured.http.Cookie;
import io.restassured.response.Response;
import ua.kpi.sc.test.api.base.BasePublicApiTest;
import ua.kpi.sc.test.api.client.auth.AuthClient;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.auth.LoginRequest;
import ua.kpi.sc.test.api.model.auth.RegisterRequest;

public abstract class BaseAuthTest extends BasePublicApiTest {

    protected final AuthClient authClient = new AuthClient();

    protected String extractCookie(Response response, String name) {
        return response.getCookie(name);
    }

    protected Cookie extractDetailedCookie(Response response, String name) {
        return response.getDetailedCookies().get(name);
    }

    protected Response registerUniqueUser() {
        RegisterRequest request = TestDataFactory.validRegisterRequest();
        Response response = authClient.register(request);
        assertCreated(response);
        return response;
    }

    protected String registerAndGetAccessToken() {
        Response response = registerUniqueUser();
        return extractCookie(response, "access_token");
    }

    protected String registerAndGetRefreshToken() {
        Response response = registerUniqueUser();
        return extractCookie(response, "refresh_token");
    }

    protected Response registerAndLogin(RegisterRequest request) {
        authClient.register(request);
        LoginRequest loginRequest = LoginRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
        return authClient.login(loginRequest);
    }
}
