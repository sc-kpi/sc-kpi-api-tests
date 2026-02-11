package ua.kpi.sc.test.api.client.webhook;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Endpoint;

public class TelegramWebhookClient extends ApiClient {

    @Step("POST /webhooks/telegram — Send Telegram webhook callback")
    public Response sendWebhook(Object payload) {
        return post(Endpoint.TELEGRAM_WEBHOOK, payload);
    }

    @Step("POST /webhooks/telegram — Send Telegram webhook with auth")
    public Response sendWebhook(Object payload, String authToken) {
        return post(Endpoint.TELEGRAM_WEBHOOK, payload, authToken);
    }
}
