package ua.kpi.sc.test.api.tests.smoke;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.base.BasePublicApiTest;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.util.AssertionHelper;

@Epic("Infrastructure")
@Feature("Health Check")
public class HealthCheckTest extends BasePublicApiTest {

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE})
    @Severity(SeverityLevel.BLOCKER)
    public void healthEndpointReturns200WithStatusUp() {
        Response response = get(Endpoint.HEALTH);

        AssertionHelper.assertStatusCode(response, 200);
        AssertionHelper.assertContentType(response, "application/json");
        AssertionHelper.assertJsonPath(response, "status", "UP");
        AssertionHelper.assertResponseTime(response, 5000);
    }

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE})
    @Severity(SeverityLevel.NORMAL)
    public void healthEndpointResponseTimeIsAcceptable() {
        Response response = get(Endpoint.HEALTH);

        AssertionHelper.assertStatusCode(response, 200);
        AssertionHelper.assertResponseTime(response, 3000);
    }
}
