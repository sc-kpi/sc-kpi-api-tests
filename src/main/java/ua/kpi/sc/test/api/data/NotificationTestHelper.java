package ua.kpi.sc.test.api.data;

import net.datafaker.Faker;
import ua.kpi.sc.test.api.client.notification.NotificationAdminClient;
import ua.kpi.sc.test.api.model.notification.BroadcastRequest;

public class NotificationTestHelper {

    private static final Faker faker = new Faker();

    private final NotificationAdminClient adminClient;
    private final String authToken;

    public NotificationTestHelper(String authToken) {
        this.adminClient = new NotificationAdminClient();
        this.authToken = authToken;
    }

    public static BroadcastRequest validBroadcastRequest() {
        return BroadcastRequest.builder()
                .titleKey("test.notification.title." + faker.lorem().characters(5, 10, false, false))
                .bodyKey("test.notification.body." + faker.lorem().characters(5, 10, false, false))
                .bodyArgs(new String[]{faker.lorem().word()})
                .category("SYSTEM")
                .build();
    }

    public void broadcastNotification() {
        adminClient.broadcast(authToken, validBroadcastRequest());
    }

    public void cleanup() {
        try {
            adminClient.cleanup(authToken, 0);
        } catch (Exception ignored) {
            // Best effort cleanup
        }
    }
}
