package ua.kpi.sc.test.api.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApiConnectionException extends TestFrameworkException {

    public ApiConnectionException(String method, String url, Throwable cause) {
        super("API connection failed: " + method + " " + url + " — "
                        + cause.getClass().getSimpleName() + ": " + cause.getMessage(),
                cause,
                buildContext(method, url));
    }

    private static Map<String, String> buildContext(String method, String url) {
        var ctx = new LinkedHashMap<String, String>();
        ctx.put("method", method);
        ctx.put("url", url);
        ctx.put("suggestion", "Verify the API is running and accessible at the configured base URL");
        return ctx;
    }
}
