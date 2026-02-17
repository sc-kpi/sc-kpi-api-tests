package ua.kpi.sc.test.api.data;

import java.util.ArrayList;
import java.util.List;

import io.restassured.response.Response;
import net.datafaker.Faker;
import ua.kpi.sc.test.api.client.ratelimit.RateLimitClient;
import ua.kpi.sc.test.api.model.ratelimit.CreateRateLimitRuleRequest;
import ua.kpi.sc.test.api.model.ratelimit.RateLimitRuleResponse;
import ua.kpi.sc.test.api.model.ratelimit.RateLimitToggleRequest;
import ua.kpi.sc.test.api.model.ratelimit.UpdateRateLimitRuleRequest;

public class RateLimitTestHelper {

    private static final Faker faker = new Faker();

    private final RateLimitClient client;
    private final String authToken;
    private final List<String> createdRuleIds = new ArrayList<>();

    public RateLimitTestHelper(RateLimitClient client, String authToken) {
        this.client = client;
        this.authToken = authToken;
    }

    public static CreateRateLimitRuleRequest validCreateRequest() {
        String name = "test." + faker.lorem().characters(5, 10, false, false);
        return CreateRateLimitRuleRequest.builder()
                .name(name)
                .description("Test rate limit rule")
                .endpointPattern("/api/v1/test/**")
                .limitPerPeriod(60)
                .periodSeconds(60)
                .burstCapacity(90)
                .scope("IP")
                .priority(1)
                .enabled(true)
                .build();
    }

    public static CreateRateLimitRuleRequest createRequestWithName(String name) {
        return CreateRateLimitRuleRequest.builder()
                .name(name)
                .description("Test rate limit rule: " + name)
                .endpointPattern("/api/v1/test/**")
                .limitPerPeriod(60)
                .periodSeconds(60)
                .burstCapacity(90)
                .scope("IP")
                .priority(1)
                .enabled(true)
                .build();
    }

    public static CreateRateLimitRuleRequest disabledRuleRequest() {
        String name = "test." + faker.lorem().characters(5, 10, false, false);
        return CreateRateLimitRuleRequest.builder()
                .name(name)
                .description("Disabled test rule")
                .endpointPattern("/api/v1/test/**")
                .limitPerPeriod(60)
                .periodSeconds(60)
                .burstCapacity(90)
                .scope("IP")
                .priority(1)
                .enabled(false)
                .build();
    }

    public static CreateRateLimitRuleRequest globalScopeRequest() {
        String name = "test.global." + faker.lorem().characters(5, 10, false, false);
        return CreateRateLimitRuleRequest.builder()
                .name(name)
                .description("Global scope test rule")
                .endpointPattern("/api/v1/**")
                .limitPerPeriod(120)
                .periodSeconds(60)
                .burstCapacity(180)
                .scope("GLOBAL")
                .priority(1)
                .enabled(true)
                .build();
    }

    public static CreateRateLimitRuleRequest tierScopeRequest(int targetTier) {
        String name = "test.tier." + faker.lorem().characters(5, 10, false, false);
        return CreateRateLimitRuleRequest.builder()
                .name(name)
                .description("Tier scope test rule")
                .endpointPattern("/api/v1/**")
                .limitPerPeriod(60)
                .periodSeconds(60)
                .burstCapacity(90)
                .scope("TIER")
                .targetTier(targetTier)
                .priority(1)
                .enabled(true)
                .build();
    }

    public static RateLimitToggleRequest toggleRequest(boolean enabled) {
        return RateLimitToggleRequest.builder()
                .enabled(enabled)
                .build();
    }

    public static UpdateRateLimitRuleRequest updateDescription(String description) {
        return UpdateRateLimitRuleRequest.builder()
                .description(description)
                .build();
    }

    public RateLimitRuleResponse createRule(CreateRateLimitRuleRequest request) {
        Response response = client.createRule(request, authToken);
        RateLimitRuleResponse rule = response.as(RateLimitRuleResponse.class);
        createdRuleIds.add(rule.getId());
        return rule;
    }

    public RateLimitRuleResponse createDefaultRule() {
        return createRule(validCreateRequest());
    }

    public void cleanup() {
        for (String id : createdRuleIds) {
            try {
                client.deleteRule(id, authToken);
            } catch (Exception ignored) {
                // Best effort cleanup
            }
        }
        createdRuleIds.clear();
    }
}
