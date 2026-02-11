package ua.kpi.sc.test.api.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import ua.kpi.sc.test.api.config.Config;

import java.util.concurrent.atomic.AtomicInteger;

public class RetryListener implements IRetryAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(RetryListener.class);
    private final AtomicInteger retryCount = new AtomicInteger(0);

    @Override
    public boolean retry(ITestResult result) {
        int maxRetries = Config.retry().getMaxAttempts();
        if (maxRetries <= 0) {
            return false;
        }

        int currentRetry = retryCount.incrementAndGet();
        if (currentRetry <= maxRetries) {
            long backoff = Config.retry().getBackoffMs();
            log.warn("Retrying test {}.{} (attempt {}/{}), backoff {}ms",
                    result.getTestClass().getName(),
                    result.getMethod().getMethodName(),
                    currentRetry, maxRetries, backoff);

            try {
                Thread.sleep(backoff);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return true;
        }

        return false;
    }
}
