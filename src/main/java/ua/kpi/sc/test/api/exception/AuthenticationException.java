package ua.kpi.sc.test.api.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class AuthenticationException extends TestFrameworkException {

    public AuthenticationException(String email, int httpStatus, String responseBody) {
        super("Authentication failed for " + email + ": HTTP " + httpStatus,
                null,
                buildContext(email, httpStatus, responseBody));
    }

    public AuthenticationException(String email, Throwable cause) {
        super("Authentication failed for " + email + ": " + cause.getMessage(),
                cause,
                Map.of("email", email));
    }

    private static Map<String, String> buildContext(String email, int httpStatus, String responseBody) {
        var ctx = new LinkedHashMap<String, String>();
        ctx.put("email", email);
        ctx.put("httpStatus", String.valueOf(httpStatus));
        if (responseBody != null) {
            ctx.put("responseBody", truncate(responseBody, 2000));
        }
        return ctx;
    }

    private static String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...(truncated)";
    }
}
