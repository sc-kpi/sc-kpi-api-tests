package ua.kpi.sc.test.api.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class SerializationException extends TestFrameworkException {

    public enum Direction {
        SERIALIZE, DESERIALIZE
    }

    public SerializationException(Direction direction, String typeName, Throwable cause) {
        this(direction, typeName, null, cause);
    }

    public SerializationException(Direction direction, String typeName, String jsonSnippet, Throwable cause) {
        super(buildMessage(direction, typeName),
                cause,
                buildContext(direction, typeName, jsonSnippet));
    }

    private static String buildMessage(Direction direction, String typeName) {
        return switch (direction) {
            case SERIALIZE -> "Failed to serialize " + typeName + " to JSON";
            case DESERIALIZE -> "Failed to deserialize JSON to " + typeName;
        };
    }

    private static Map<String, String> buildContext(Direction direction, String typeName, String jsonSnippet) {
        var ctx = new LinkedHashMap<String, String>();
        ctx.put("direction", direction.name());
        ctx.put("type", typeName);
        if (jsonSnippet != null) {
            ctx.put("jsonSnippet", truncate(jsonSnippet, 500));
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
