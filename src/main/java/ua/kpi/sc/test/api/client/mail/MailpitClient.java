package ua.kpi.sc.test.api.client.mail;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ua.kpi.sc.test.api.config.Config;
import ua.kpi.sc.test.api.util.JsonHelper;

import static io.restassured.RestAssured.given;

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

    @Step("Get MailPit message by ID: {id}")
    public Response getMessage(String id) {
        return given()
                .config(JsonHelper.configWithJackson3())
                .baseUri(baseUrl())
                .get("/api/v1/message/{id}", id);
    }

    @Step("Wait for MailPit message for recipient: {recipient} (timeout: {timeoutSeconds}s)")
    public Response waitForMessage(String recipient, int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;

        while (System.currentTimeMillis() < deadline) {
            Response response = searchMessages(recipient);
            if (response.statusCode() == 200) {
                int count = response.jsonPath().getInt("messages_count");
                if (count > 0) {
                    return response;
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for MailPit message", e);
            }
        }

        throw new AssertionError("No message received for " + recipient
                + " within " + timeoutSeconds + " seconds");
    }

    @Step("Delete all MailPit messages")
    public Response deleteAllMessages() {
        return given()
                .baseUri(baseUrl())
                .delete("/api/v1/messages");
    }
}
