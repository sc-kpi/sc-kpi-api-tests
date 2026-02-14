package ua.kpi.sc.test.api.client.mail;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.awaitility.core.ConditionTimeoutException;
import ua.kpi.sc.test.api.config.Config;
import ua.kpi.sc.test.api.exception.MailpitTimeoutException;
import ua.kpi.sc.test.api.model.mail.MailpitMessageResponse;
import ua.kpi.sc.test.api.model.mail.MailpitSearchResponse;
import ua.kpi.sc.test.api.util.JsonHelper;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

public class MailpitClient {

    private String baseUrl() {
        return Config.mailpit().getBaseUrl();
    }

    @Step("Search MailPit messages for recipient: {recipient}")
    public Response searchMessages(String recipient) {
        return given()
                .config(JsonHelper.configWithJackson3())
                .baseUri(baseUrl())
                .queryParam("query", "to:" + recipient)
                .get("/api/v1/search");
    }

    @Step("Search MailPit messages (typed) for recipient: {recipient}")
    public MailpitSearchResponse searchMessagesTyped(String recipient) {
        return searchMessages(recipient).as(MailpitSearchResponse.class);
    }

    @Step("Get MailPit message by ID: {id}")
    public Response getMessage(String id) {
        return given()
                .config(JsonHelper.configWithJackson3())
                .baseUri(baseUrl())
                .get("/api/v1/message/{id}", id);
    }

    @Step("Get MailPit message (typed) by ID: {id}")
    public MailpitMessageResponse getMessageTyped(String id) {
        return getMessage(id).as(MailpitMessageResponse.class);
    }

    @Step("Wait for MailPit message for recipient: {recipient}")
    public Response waitForMessage(String recipient) {
        return waitForMessage(recipient, Config.mailpit().getTimeoutSeconds());
    }

    @Step("Wait for MailPit message for recipient: {recipient} (timeout: {timeoutSeconds}s)")
    public Response waitForMessage(String recipient, int timeoutSeconds) {
        var lastStatusCode = new AtomicInteger(0);

        try {
            return await()
                    .atMost(Duration.ofSeconds(timeoutSeconds))
                    .pollInterval(Duration.ofMillis(Config.mailpit().getPollIntervalMs()))
                    .pollDelay(Duration.ZERO)
                    .until(() -> {
                        Response response = searchMessages(recipient);
                        lastStatusCode.set(response.statusCode());
                        return response;
                    }, response ->
                            response.statusCode() == 200
                                    && response.jsonPath().getInt("messages_count") > 0);
        } catch (ConditionTimeoutException e) {
            throw new MailpitTimeoutException(recipient, baseUrl(), timeoutSeconds, lastStatusCode.get());
        }
    }

    @Step("Wait for MailPit message (typed) for recipient: {recipient}")
    public MailpitSearchResponse waitForMessageTyped(String recipient) {
        return waitForMessage(recipient).as(MailpitSearchResponse.class);
    }

    @Step("Wait for MailPit message (typed) for recipient: {recipient} (timeout: {timeoutSeconds}s)")
    public MailpitSearchResponse waitForMessageTyped(String recipient, int timeoutSeconds) {
        return waitForMessage(recipient, timeoutSeconds).as(MailpitSearchResponse.class);
    }

    @Step("Delete all MailPit messages")
    public Response deleteAllMessages() {
        return given()
                .baseUri(baseUrl())
                .delete("/api/v1/messages");
    }
}
