package ua.kpi.sc.test.api.base;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import ua.kpi.sc.test.api.annotation.Authentication;
import ua.kpi.sc.test.api.auth.AuthContext;
import ua.kpi.sc.test.api.auth.AuthContextResolver;
import ua.kpi.sc.test.api.auth.AuthManager;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Config;
import ua.kpi.sc.test.api.util.AssertionHelper;
import ua.kpi.sc.test.api.util.JsonHelper;

import java.lang.reflect.Method;
import java.util.UUID;

public abstract class BaseApiTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final ApiClient apiClient = new ApiClient();
    protected String authToken;

    @BeforeClass(alwaysRun = true)
    public void baseSetUp() {
        RestAssured.baseURI = Config.baseUrl();
        RestAssured.config = JsonHelper.configWithJackson3();
        log.info("Test class initialized: {}", getClass().getSimpleName());
    }

    @BeforeMethod(alwaysRun = true)
    public void baseBeforeMethod(Method method, ITestResult testResult) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        String testName = getClass().getSimpleName() + "." + method.getName();

        Allure.parameter("requestId", requestId);
        Allure.parameter("testName", testName);

        AuthContext authContext = AuthContextResolver.resolve(method, getClass());
        if (authContext.enabled()) {
            authToken = AuthManager.getToken(authContext);
            log.debug("Auth token acquired for tier: {}", authContext.tier());
        } else {
            authToken = null;
        }
    }

    @AfterMethod(alwaysRun = true)
    public void baseAfterMethod(ITestResult result) {
        String status = switch (result.getStatus()) {
            case ITestResult.SUCCESS -> "PASSED";
            case ITestResult.FAILURE -> "FAILED";
            case ITestResult.SKIP -> "SKIPPED";
            default -> "UNKNOWN";
        };
        log.info("Test {}.{} — {} ({}ms)",
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName(),
                status,
                result.getEndMillis() - result.getStartMillis());
    }

    // HTTP helper methods
    protected Response get(String path) {
        return apiClient.get(path);
    }

    protected Response get(String path, String token) {
        return apiClient.get(path, token);
    }

    protected Response post(String path, Object body) {
        return apiClient.post(path, body);
    }

    protected Response post(String path, Object body, String token) {
        return apiClient.post(path, body, token);
    }

    protected Response put(String path, Object body) {
        return apiClient.put(path, body);
    }

    protected Response put(String path, Object body, String token) {
        return apiClient.put(path, body, token);
    }

    protected Response patch(String path, Object body) {
        return apiClient.patch(path, body);
    }

    protected Response patch(String path, Object body, String token) {
        return apiClient.patch(path, body, token);
    }

    protected Response delete(String path) {
        return apiClient.delete(path);
    }

    protected Response delete(String path, String token) {
        return apiClient.delete(path, token);
    }

    // Assertion helpers
    protected void assertStatus(Response response, int expectedStatus) {
        AssertionHelper.assertStatusCode(response, expectedStatus);
    }

    protected void assertOk(Response response) {
        AssertionHelper.assertStatusCode(response, 200);
    }

    protected void assertCreated(Response response) {
        AssertionHelper.assertStatusCode(response, 201);
    }

    protected void assertNoContent(Response response) {
        AssertionHelper.assertStatusCode(response, 204);
    }

    @Step("{description}")
    protected void step(String description, Runnable action) {
        action.run();
    }
}
