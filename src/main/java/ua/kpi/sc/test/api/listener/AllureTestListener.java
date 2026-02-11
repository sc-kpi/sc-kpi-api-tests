package ua.kpi.sc.test.api.listener;

import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;
import ua.kpi.sc.test.api.exception.TestFrameworkException;
import ua.kpi.sc.test.api.util.AllureHelper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

public class AllureTestListener implements ITestListener {

    private static final Logger log = LoggerFactory.getLogger(AllureTestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        log.info("Starting test: {}", testName);
        Allure.getLifecycle().updateTestCase(tc ->
                tc.getLabels().add(new io.qameta.allure.model.Label().setName("thread").setValue(Thread.currentThread().getName()))
        );
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("PASSED: {}.{} ({}ms)",
                result.getTestClass().getName(),
                result.getMethod().getMethodName(),
                result.getEndMillis() - result.getStartMillis());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        log.error("FAILED: {} ({}ms)", testName,
                result.getEndMillis() - result.getStartMillis());

        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            AllureHelper.attachText("Stack Trace", getFullStackTrace(throwable));

            if (throwable instanceof TestFrameworkException tfe) {
                AllureHelper.attachText("Detailed Message", tfe.getDetailedMessage());
                AllureHelper.attachJson("Exception Context", formatContextAsJson(tfe.getContext()));
            }
        }

        Object response = result.getAttribute("lastResponse");
        if (response != null) {
            AllureHelper.attachJson("Response Body", response.toString());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("SKIPPED: {}.{}",
                result.getTestClass().getName(),
                result.getMethod().getMethodName());
    }

    private String getFullStackTrace(Throwable throwable) {
        var sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private String formatContextAsJson(Map<String, String> context) {
        // Manual JSON formatting to avoid circular failure if Jackson is broken
        var sb = new StringBuilder();
        sb.append("{\n");
        var entries = context.entrySet().iterator();
        while (entries.hasNext()) {
            var entry = entries.next();
            sb.append("  \"").append(escapeJson(entry.getKey())).append("\": \"")
                    .append(escapeJson(entry.getValue())).append("\"");
            if (entries.hasNext()) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "null";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
