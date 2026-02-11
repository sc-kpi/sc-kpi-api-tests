package ua.kpi.sc.test.api.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestConfig {

    private String baseUrl = "http://localhost:8080";
    private int timeout = 30000;
    private ExecutionConfig execution = new ExecutionConfig();
    private AuthConfig auth = new AuthConfig();
    private RetryConfig retry = new RetryConfig();
    private CleanupConfig cleanup = new CleanupConfig();

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExecutionConfig {
        private String parallel = "none";
        private int threadCount = 1;
        private String groups = "";
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthConfig {
        private boolean enabled = false;
        private String tokenEndpoint = "/api/v1/auth/login";
        private String refreshEndpoint = "/api/v1/auth/refresh";
        private Map<String, TierCredentials> tierCredentials = Map.of();
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TierCredentials {
        private String email;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RetryConfig {
        private int maxAttempts = 0;
        private long backoffMs = 1000;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CleanupConfig {
        private boolean enabled = true;
        private String strategy = "after_suite";
    }
}
