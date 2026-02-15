package ua.kpi.sc.test.api.data;

import java.util.ArrayList;
import java.util.List;

import io.restassured.response.Response;
import net.datafaker.Faker;
import ua.kpi.sc.test.api.client.featureflag.FeatureFlagClient;
import ua.kpi.sc.test.api.model.featureflag.CreateFeatureFlagRequest;
import ua.kpi.sc.test.api.model.featureflag.CreateOverrideRequest;
import ua.kpi.sc.test.api.model.featureflag.FeatureFlagResponse;
import ua.kpi.sc.test.api.model.featureflag.ToggleFeatureFlagRequest;

public class FeatureFlagTestHelper {

    private static final Faker faker = new Faker();

    private final FeatureFlagClient client;
    private final String authToken;
    private final List<String> createdFlagIds = new ArrayList<>();

    public FeatureFlagTestHelper(FeatureFlagClient client, String authToken) {
        this.client = client;
        this.authToken = authToken;
    }

    public static CreateFeatureFlagRequest validCreateRequest() {
        String key = "test." + faker.lorem().characters(5, 10, false, false);
        return CreateFeatureFlagRequest.builder()
                .key(key)
                .name("Test Flag " + faker.lorem().word())
                .description(faker.lorem().sentence())
                .enabled(true)
                .rolloutPercentage(100)
                .build();
    }

    public static CreateFeatureFlagRequest createRequestWithKey(String key) {
        return CreateFeatureFlagRequest.builder()
                .key(key)
                .name("Test Flag " + faker.lorem().word())
                .description(faker.lorem().sentence())
                .enabled(true)
                .rolloutPercentage(100)
                .build();
    }

    public static CreateFeatureFlagRequest disabledFlagRequest() {
        String key = "test." + faker.lorem().characters(5, 10, false, false);
        return CreateFeatureFlagRequest.builder()
                .key(key)
                .name("Disabled Flag " + faker.lorem().word())
                .description(faker.lorem().sentence())
                .enabled(false)
                .rolloutPercentage(0)
                .build();
    }

    public static CreateOverrideRequest tierOverride(int tierLevel, boolean enabled) {
        return CreateOverrideRequest.builder()
                .overrideType("TIER")
                .tierLevel(tierLevel)
                .enabled(enabled)
                .build();
    }

    public static CreateOverrideRequest userOverride(String userId, boolean enabled) {
        return CreateOverrideRequest.builder()
                .overrideType("USER")
                .userId(userId)
                .enabled(enabled)
                .build();
    }

    public static ToggleFeatureFlagRequest toggleRequest(boolean enabled) {
        return ToggleFeatureFlagRequest.builder()
                .enabled(enabled)
                .reason("Test toggle")
                .build();
    }

    public FeatureFlagResponse createFlag(CreateFeatureFlagRequest request) {
        Response response = client.createFlag(request, authToken);
        FeatureFlagResponse flag = response.as(FeatureFlagResponse.class);
        createdFlagIds.add(flag.getId());
        return flag;
    }

    public FeatureFlagResponse createDefaultFlag() {
        return createFlag(validCreateRequest());
    }

    public void cleanup() {
        for (String id : createdFlagIds) {
            try {
                client.deleteFlag(id, authToken);
            } catch (Exception ignored) {
                // Best effort cleanup
            }
        }
        createdFlagIds.clear();
    }
}
