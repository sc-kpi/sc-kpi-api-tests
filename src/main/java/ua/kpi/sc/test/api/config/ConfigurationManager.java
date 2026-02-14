package ua.kpi.sc.test.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;
import ua.kpi.sc.test.api.exception.ConfigurationException;

import java.io.InputStream;

public final class ConfigurationManager {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationManager.class);
    private static volatile TestConfig config;

    private ConfigurationManager() {}

    public static TestConfig getConfig() {
        if (config == null) {
            synchronized (ConfigurationManager.class) {
                if (config == null) {
                    config = loadConfig();
                    applySystemPropertyOverrides(config);
                    log.info("Configuration loaded: baseUrl={}, authEnabled={}", config.getBaseUrl(), config.getAuth().isEnabled());
                }
            }
        }
        return config;
    }

    private static TestConfig loadConfig() {
        ObjectMapper mapper = YAMLMapper.builder().build();
        String env = System.getProperty("env", "");

        TestConfig baseConfig = loadYaml(mapper, "application.yml");
        if (baseConfig == null) {
            throw new ConfigurationException("Required configuration file not found: application.yml");
        }

        if (!env.isEmpty()) {
            String profileFile = "application-" + env + ".yml";
            TestConfig profileConfig = loadYaml(mapper, profileFile);
            if (profileConfig != null) {
                mergeConfig(baseConfig, profileConfig);
            }
        }

        return baseConfig;
    }

    private static TestConfig loadYaml(ObjectMapper mapper, String fileName) {
        try (InputStream is = ConfigurationManager.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                log.warn("Configuration file not found: {}", fileName);
                return null;
            }
            return mapper.readValue(is, TestConfig.class);
        } catch (Exception e) {
            throw new ConfigurationException(fileName, e);
        }
    }

    private static void mergeConfig(TestConfig base, TestConfig overlay) {
        if (overlay.getBaseUrl() != null && !overlay.getBaseUrl().equals("http://localhost:8080")) {
            base.setBaseUrl(overlay.getBaseUrl());
        }
        if (overlay.getTimeout() != 30000) {
            base.setTimeout(overlay.getTimeout());
        }
        if (overlay.getAuth().isEnabled()) {
            base.getAuth().setEnabled(true);
        }
        if (overlay.getAuth().getTierCredentials() != null && !overlay.getAuth().getTierCredentials().isEmpty()) {
            base.getAuth().setTierCredentials(overlay.getAuth().getTierCredentials());
        }
        if (overlay.getRetry().getMaxAttempts() != 0) {
            base.getRetry().setMaxAttempts(overlay.getRetry().getMaxAttempts());
        }
        if (overlay.getExecution().getThreadCount() != 1) {
            base.getExecution().setThreadCount(overlay.getExecution().getThreadCount());
        }
        if (overlay.getMailpit() != null) {
            if (overlay.getMailpit().getBaseUrl() != null && !overlay.getMailpit().getBaseUrl().isEmpty()) {
                base.getMailpit().setBaseUrl(overlay.getMailpit().getBaseUrl());
            }
            if (overlay.getMailpit().getTimeoutSeconds() != 15) {
                base.getMailpit().setTimeoutSeconds(overlay.getMailpit().getTimeoutSeconds());
            }
            if (overlay.getMailpit().getPollIntervalMs() != 1000) {
                base.getMailpit().setPollIntervalMs(overlay.getMailpit().getPollIntervalMs());
            }
        }
    }

    private static void applySystemPropertyOverrides(TestConfig config) {
        String baseUrl = System.getProperty("baseUrl");
        if (baseUrl != null) {
            config.setBaseUrl(baseUrl);
        }

        String timeout = System.getProperty("timeout");
        if (timeout != null) {
            config.setTimeout(Integer.parseInt(timeout));
        }

        String authEnabled = System.getProperty("auth.enabled");
        if (authEnabled != null) {
            config.getAuth().setEnabled(Boolean.parseBoolean(authEnabled));
        }

        String parallel = System.getProperty("parallel");
        if (parallel != null) {
            config.getExecution().setParallel(parallel);
        }

        String threadCount = System.getProperty("threadCount");
        if (threadCount != null) {
            config.getExecution().setThreadCount(Integer.parseInt(threadCount));
        }

        String mailpitTimeout = System.getProperty("mailpit.timeout");
        if (mailpitTimeout != null) {
            config.getMailpit().setTimeoutSeconds(Integer.parseInt(mailpitTimeout));
        }

        String mailpitPollInterval = System.getProperty("mailpit.pollInterval");
        if (mailpitPollInterval != null) {
            config.getMailpit().setPollIntervalMs(Long.parseLong(mailpitPollInterval));
        }
    }

    static void reset() {
        config = null;
    }
}
