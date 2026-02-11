package ua.kpi.sc.test.api.exception;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class TestFrameworkException extends RuntimeException {

    private final Instant timestamp;
    private final Map<String, String> context;

    protected TestFrameworkException(String message) {
        this(message, null, Map.of());
    }

    protected TestFrameworkException(String message, Throwable cause) {
        this(message, cause, Map.of());
    }

    protected TestFrameworkException(String message, Throwable cause, Map<String, String> context) {
        super(message, cause);
        this.timestamp = Instant.now();
        this.context = Collections.unmodifiableMap(new LinkedHashMap<>(context));
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getContext() {
        return context;
    }

    public String getDetailedMessage() {
        var sb = new StringBuilder();
        sb.append("[").append(getClass().getSimpleName()).append("] ").append(getMessage()).append("\n");
        sb.append("Timestamp: ").append(timestamp).append("\n");

        if (!context.isEmpty()) {
            sb.append("Context:\n");
            context.forEach((key, value) ->
                    sb.append("  ").append(key).append(": ").append(value).append("\n"));
        }

        if (getCause() != null) {
            sb.append("Cause: ").append(getCause().getClass().getName())
                    .append(": ").append(getCause().getMessage()).append("\n");
        }

        return sb.toString();
    }
}
