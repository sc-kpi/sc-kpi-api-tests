package ua.kpi.sc.test.api.util;

import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.kpi.sc.test.api.config.Config;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class AllureHelper {

    private static final Logger log = LoggerFactory.getLogger(AllureHelper.class);

    private AllureHelper() {}

    public static void writeEnvironmentProperties() {
        String resultsDir = System.getProperty("allure.results.directory", "build/allure-results");
        Path resultsPath = Paths.get(resultsDir);

        try {
            Files.createDirectories(resultsPath);

            Properties props = new Properties();
            props.setProperty("Base URL", Config.baseUrl());
            props.setProperty("Timeout", String.valueOf(Config.timeout()));
            props.setProperty("Auth Enabled", String.valueOf(Config.isAuthEnabled()));
            props.setProperty("Parallel Mode", Config.execution().getParallel());
            props.setProperty("Thread Count", String.valueOf(Config.execution().getThreadCount()));
            props.setProperty("Environment Profile", System.getProperty("env", "default"));
            props.setProperty("Retry Max Attempts", String.valueOf(Config.retry().getMaxAttempts()));
            props.setProperty("Retry Backoff (ms)", String.valueOf(Config.retry().getBackoffMs()));
            props.setProperty("Cleanup Enabled", String.valueOf(Config.cleanup().isEnabled()));
            props.setProperty("Java Version", System.getProperty("java.version", "unknown"));
            props.setProperty("OS", System.getProperty("os.name", "unknown"));

            Path envFile = resultsPath.resolve("environment.properties");
            try (FileOutputStream fos = new FileOutputStream(envFile.toFile())) {
                props.store(fos, "Allure Environment Properties");
            }
            log.info("Allure environment properties written to {}", envFile);
        } catch (IOException e) {
            // Intentionally non-fatal: Allure properties are supplementary metadata
            log.warn("Failed to write Allure environment properties", e);
        }
    }

    public static void attachText(String name, String content) {
        Allure.addAttachment(name, "text/plain", content);
    }

    public static void attachJson(String name, String json) {
        Allure.addAttachment(name, "application/json", json);
    }

    public static void step(String name, Runnable action) {
        Allure.step(name, () -> {
            action.run();
        });
    }
}
