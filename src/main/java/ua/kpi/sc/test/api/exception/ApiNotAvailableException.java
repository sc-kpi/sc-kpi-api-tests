package ua.kpi.sc.test.api.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApiNotAvailableException extends TestFrameworkException {

    public ApiNotAvailableException(String baseUrl, Throwable cause) {
        super("API is not available at " + baseUrl + ". Start the API before running tests.",
                cause,
                buildContext(baseUrl));
    }

    private static Map<String, String> buildContext(String baseUrl) {
        var ctx = new LinkedHashMap<String, String>();
        ctx.put("baseUrl", baseUrl);
        ctx.put("healthEndpoint", baseUrl + "/actuator/health");
        ctx.put("suggestion", "Start the API with ./gradlew bootRun in sc-kpi-api/");
        return ctx;
    }
}
