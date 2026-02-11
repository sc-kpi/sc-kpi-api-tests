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
@Feature("API Documentation")
public class SwaggerAvailabilityTest extends BasePublicApiTest {

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE})
    @Severity(SeverityLevel.NORMAL)
    public void swaggerUiIsAccessible() {
        Response response = get(Endpoint.SWAGGER_UI);

        AssertionHelper.assertStatusCode(response, 200);
    }

    @Test(groups = {TestGroup.SMOKE, TestGroup.POSITIVE})
    @Severity(SeverityLevel.NORMAL)
    public void apiDocsEndpointReturnsOpenApiSpec() {
        Response response = get(Endpoint.API_DOCS);

        AssertionHelper.assertStatusCode(response, 200);
        AssertionHelper.assertContentType(response, "application/json");
        AssertionHelper.assertJsonPathNotNull(response, "openapi");
        AssertionHelper.assertJsonPathNotNull(response, "info");
        AssertionHelper.assertJsonPathNotNull(response, "paths");
    }
}
