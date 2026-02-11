package ua.kpi.sc.test.api.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IExecutionListener;
import ua.kpi.sc.test.api.auth.AuthManager;
import ua.kpi.sc.test.api.config.Config;
import ua.kpi.sc.test.api.data.CleanupRegistry;
import ua.kpi.sc.test.api.exception.CleanupException;
import ua.kpi.sc.test.api.util.AllureHelper;

public class ExecutionListener implements IExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(ExecutionListener.class);

    @Override
    public void onExecutionStart() {
        log.info("=== Test Execution Started ===");
        log.info("Base URL: {}", Config.baseUrl());
        log.info("Auth enabled: {}", Config.isAuthEnabled());
        log.info("Parallel mode: {}, threads: {}",
                Config.execution().getParallel(),
                Config.execution().getThreadCount());
        AllureHelper.writeEnvironmentProperties();
    }

    @Override
    public void onExecutionFinish() {
        log.info("=== Test Execution Finished ===");

        if (Config.cleanup().isEnabled()) {
            log.info("Running cleanup ({} actions registered)", CleanupRegistry.size());
            try {
                CleanupRegistry.executeAll();
            } catch (CleanupException e) {
                log.error("Some cleanup actions failed: {}", e.getMessage());
            }
        }

        AuthManager.clearCache();
        log.info("=== Cleanup Complete ===");
    }
}
