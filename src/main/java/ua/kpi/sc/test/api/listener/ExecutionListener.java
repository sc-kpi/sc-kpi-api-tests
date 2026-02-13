package ua.kpi.sc.test.api.listener;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IExecutionListener;
import ua.kpi.sc.test.api.auth.AuthManager;
import ua.kpi.sc.test.api.config.Config;
import ua.kpi.sc.test.api.data.CleanupRegistry;
import ua.kpi.sc.test.api.exception.ApiNotAvailableException;
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
        verifyApiAvailability();
    }

    private void verifyApiAvailability() {
        String baseUrl = Config.baseUrl();
        String healthUrl = baseUrl + "/actuator/health";
        log.info("Checking API availability at {}", healthUrl);

        try (var client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(healthUrl))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("API health check: HTTP {} — {}", response.statusCode(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiNotAvailableException(baseUrl, e);
        } catch (Exception e) {
            throw new ApiNotAvailableException(baseUrl, e);
        }
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
