package ua.kpi.sc.test.api.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class MailpitTimeoutException extends TestFrameworkException {

    public MailpitTimeoutException(String recipient, String baseUrl, int timeoutSeconds, int lastStatusCode) {
        super("No email received for " + recipient + " within " + timeoutSeconds + " seconds",
                null,
                buildContext(recipient, baseUrl, timeoutSeconds, lastStatusCode));
    }

    private static Map<String, String> buildContext(String recipient, String baseUrl, int timeoutSeconds, int lastStatusCode) {
        var ctx = new LinkedHashMap<String, String>();
        ctx.put("recipient", recipient);
        ctx.put("mailpitUrl", baseUrl);
        ctx.put("timeoutSeconds", String.valueOf(timeoutSeconds));
        ctx.put("lastStatusCode", String.valueOf(lastStatusCode));
        ctx.put("suggestion", "Check that Mailpit is running at " + baseUrl + " and the application is sending emails");
        return ctx;
    }
}
