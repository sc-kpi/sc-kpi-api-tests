package ua.kpi.sc.test.api.exception;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CleanupException extends TestFrameworkException {

    public record CleanupFailure(String description, Throwable cause) {}

    public CleanupException(int totalActions, int failedCount, List<CleanupFailure> failures) {
        super(failedCount + " of " + totalActions + " cleanup actions failed",
                null,
                buildContext(totalActions, failedCount, failures));

        for (CleanupFailure failure : failures) {
            addSuppressed(failure.cause());
        }
    }

    private static Map<String, String> buildContext(int totalActions, int failedCount,
                                                    List<CleanupFailure> failures) {
        var ctx = new java.util.LinkedHashMap<String, String>();
        ctx.put("totalActions", String.valueOf(totalActions));
        ctx.put("failedCount", String.valueOf(failedCount));
        ctx.put("failedDescriptions", failures.stream()
                .map(CleanupFailure::description)
                .collect(Collectors.joining(", ")));
        return ctx;
    }
}
