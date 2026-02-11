package ua.kpi.sc.test.api.config;

public final class Config {

    private Config() {}

    public static String baseUrl() {
        return ConfigurationManager.getConfig().getBaseUrl();
    }

    public static int timeout() {
        return ConfigurationManager.getConfig().getTimeout();
    }

    public static boolean isAuthEnabled() {
        return ConfigurationManager.getConfig().getAuth().isEnabled();
    }

    public static TestConfig.AuthConfig auth() {
        return ConfigurationManager.getConfig().getAuth();
    }

    public static TestConfig.ExecutionConfig execution() {
        return ConfigurationManager.getConfig().getExecution();
    }

    public static TestConfig.RetryConfig retry() {
        return ConfigurationManager.getConfig().getRetry();
    }

    public static TestConfig.CleanupConfig cleanup() {
        return ConfigurationManager.getConfig().getCleanup();
    }

    public static TestConfig get() {
        return ConfigurationManager.getConfig();
    }
}
