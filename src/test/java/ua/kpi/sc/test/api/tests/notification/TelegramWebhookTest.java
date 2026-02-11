package ua.kpi.sc.test.api.tests.notification;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.base.BasePublicApiTest;
import ua.kpi.sc.test.api.config.TestGroup;

@Epic("Notifications")
@Feature("Telegram Webhook API")
public class TelegramWebhookTest extends BasePublicApiTest {

    @Test(enabled = false, groups = {TestGroup.POSITIVE},
            description = "Telegram webhook receives callback")
    public void telegramWebhookReceivesCallback() {
        // TODO: Implement when /api/v1/webhooks/telegram POST is ready
    }

    @Test(enabled = false, groups = {TestGroup.NEGATIVE},
            description = "Invalid webhook payload returns 400")
    public void invalidWebhookPayloadReturns400() {
        // TODO: Implement when /api/v1/webhooks/telegram POST is ready
    }
}
