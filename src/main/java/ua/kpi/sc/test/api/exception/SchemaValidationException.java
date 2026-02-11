package ua.kpi.sc.test.api.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class SchemaValidationException extends TestFrameworkException {

    public SchemaValidationException(String schemaPath, String endpoint, String responseBody, Throwable cause) {
        super("Schema validation failed for " + endpoint + " against " + schemaPath,
                cause,
                buildContext(schemaPath, endpoint, responseBody));
    }

    private static Map<String, String> buildContext(String schemaPath, String endpoint, String responseBody) {
        var ctx = new LinkedHashMap<String, String>();
        ctx.put("schemaPath", schemaPath);
        ctx.put("endpoint", endpoint);
        if (responseBody != null) {
            ctx.put("responseBody", responseBody);
        }
        return ctx;
    }
}
