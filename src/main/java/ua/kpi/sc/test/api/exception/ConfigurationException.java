package ua.kpi.sc.test.api.exception;

import java.util.Map;

public class ConfigurationException extends TestFrameworkException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String fileName, Throwable cause) {
        super("Failed to load configuration from " + fileName,
                cause,
                Map.of("fileName", fileName));
    }
}
