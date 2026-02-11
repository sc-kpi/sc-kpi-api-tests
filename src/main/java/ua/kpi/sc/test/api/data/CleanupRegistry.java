package ua.kpi.sc.test.api.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.kpi.sc.test.api.exception.CleanupException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class CleanupRegistry {

    private static final Logger log = LoggerFactory.getLogger(CleanupRegistry.class);
    private static final ConcurrentLinkedDeque<CleanupAction> actions = new ConcurrentLinkedDeque<>();

    private CleanupRegistry() {}

    public static void register(String description, Runnable action) {
        actions.push(new CleanupAction(description, action));
        log.debug("Registered cleanup action: {}", description);
    }

    public static void executeAll() {
        int total = actions.size();
        log.info("Executing {} cleanup actions (LIFO order)", total);
        int success = 0;
        List<CleanupException.CleanupFailure> failures = new ArrayList<>();

        while (!actions.isEmpty()) {
            CleanupAction action = actions.poll();
            try {
                log.debug("Executing cleanup: {}", action.description());
                action.action().run();
                success++;
            } catch (Exception e) {
                log.error("Cleanup action failed: {}", action.description(), e);
                failures.add(new CleanupException.CleanupFailure(action.description(), e));
            }
        }

        log.info("Cleanup complete: {} succeeded, {} failed", success, failures.size());

        if (!failures.isEmpty()) {
            throw new CleanupException(total, failures.size(), failures);
        }
    }

    public static int size() {
        return actions.size();
    }

    public static void clear() {
        actions.clear();
    }

    record CleanupAction(String description, Runnable action) {}
}
