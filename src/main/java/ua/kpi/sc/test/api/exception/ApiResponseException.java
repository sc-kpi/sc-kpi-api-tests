package ua.kpi.sc.test.api.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApiResponseException extends TestFrameworkException {

    private final int httpStatus;

    public ApiResponseException(String method, String endpoint, int httpStatus,
                                String requestBody, String responseBody) {
        super("Unexpected API response: " + method + " " + endpoint + " returned HTTP " + httpStatus,
                null,
                buildContext(method, endpoint, httpStatus, requestBody, responseBody));
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    private static Map<String, String> buildContext(String method, String endpoint, int httpStatus,
                                                    String requestBody, String responseBody) {
        var ctx = new LinkedHashMap<String, String>();
        ctx.put("method", method);
        ctx.put("endpoint", endpoint);
        ctx.put("httpStatus", String.valueOf(httpStatus));
        if (requestBody != null) {
            ctx.put("requestBody", requestBody);
        }
        if (responseBody != null) {
            ctx.put("responseBody", responseBody);
        }
        return ctx;
    }
}
