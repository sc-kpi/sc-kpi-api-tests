package ua.kpi.sc.test.api.tests.featureflag;

import java.util.UUID;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.data.FeatureFlagTestHelper;
import ua.kpi.sc.test.api.model.featureflag.CreateFeatureFlagRequest;
import ua.kpi.sc.test.api.model.featureflag.FeatureFlagResponse;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

@Epic("Feature Flags")
@Feature("Flag Evaluation")
public class FeatureFlagEvaluationTest extends BaseFeatureFlagTest {

    @Test(groups = {TestGroup.SMOKE},
            description = "Evaluate all flags returns map (unauthenticated)")
    public void evaluateAllUnauthenticatedReturnsMap() {
        Response response = flagClient.evaluateAll();

        assertOk(response);
        response.then()
                .body("$", notNullValue())
                .body("$", instanceOf(java.util.Map.class));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Evaluate all flags returns map (authenticated)")
    public void evaluateAllAuthenticatedReturnsMap() {
        Response response = flagClient.evaluateAll(authToken);

        assertOk(response);
        response.then()
                .body("$", notNullValue());
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Evaluate single enabled flag returns true")
    public void evaluateSingleEnabledFlagReturnsTrue() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setEnabled(true);
        request.setRolloutPercentage(100);
        FeatureFlagResponse created = getFlagHelper().createFlag(request);

        Response response = flagClient.evaluate(created.getKey());

        assertOk(response);
        response.then()
                .body("key", equalTo(created.getKey()))
                .body("enabled", equalTo(true));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Evaluate single disabled flag returns false")
    public void evaluateSingleDisabledFlagReturnsFalse() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.disabledFlagRequest();
        FeatureFlagResponse created = getFlagHelper().createFlag(request);

        Response response = flagClient.evaluate(created.getKey());

        assertOk(response);
        response.then()
                .body("key", equalTo(created.getKey()))
                .body("enabled", equalTo(false));
    }

    @Test(groups = {TestGroup.NEGATIVE},
            description = "Evaluate non-existent flag returns 404")
    public void evaluateNonExistentFlagReturns404() {
        Response response = flagClient.evaluate("non.existent.flag");

        assertStatus(response, 404);
    }

    // ==================== Evaluate All Content Verification ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Evaluate all returns correct boolean values")
    public void evaluateAllReturnsCorrectBooleanValues() {
        CreateFeatureFlagRequest enabledReq = FeatureFlagTestHelper.validCreateRequest();
        enabledReq.setEnabled(true);
        enabledReq.setRolloutPercentage(100);
        FeatureFlagResponse enabledFlag = getFlagHelper().createFlag(enabledReq);

        CreateFeatureFlagRequest disabledReq = FeatureFlagTestHelper.validCreateRequest();
        disabledReq.setEnabled(false);
        FeatureFlagResponse disabledFlag = getFlagHelper().createFlag(disabledReq);

        Response response = flagClient.evaluateAll();

        assertOk(response);
        response.then()
                .body("'" + enabledFlag.getKey() + "'", equalTo(true))
                .body("'" + disabledFlag.getKey() + "'", equalTo(false));
    }

    // ==================== Override Evaluation ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Evaluate all with tier override returns overridden value")
    public void evaluateAllWithTierOverride_returnsOverriddenValue() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setEnabled(true);
        request.setRolloutPercentage(100);
        FeatureFlagResponse created = getFlagHelper().createFlag(request);

        // Add a tier override for the authenticated user's tier that disables the flag
        flagClient.addOverride(created.getId(), FeatureFlagTestHelper.tierOverride(5, false), authToken);

        // Authenticated user with ADMIN tier (level 5) should see the override
        Response response = flagClient.evaluateAll(authToken);

        assertOk(response);
        response.then()
                .body("'" + created.getKey() + "'", equalTo(false));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Evaluate single with tier override returns overridden value")
    public void evaluateSingleWithTierOverride_returnsOverriddenValue() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setEnabled(true);
        request.setRolloutPercentage(100);
        FeatureFlagResponse created = getFlagHelper().createFlag(request);

        flagClient.addOverride(created.getId(), FeatureFlagTestHelper.tierOverride(5, false), authToken);

        Response response = flagClient.evaluate(created.getKey(), authToken);

        assertOk(response);
        response.then()
                .body("key", equalTo(created.getKey()))
                .body("enabled", equalTo(false));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Evaluate unauthenticated returns global defaults only")
    public void evaluateAllUnauthenticated_returnsGlobalDefaults() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setEnabled(true);
        request.setRolloutPercentage(100);
        FeatureFlagResponse created = getFlagHelper().createFlag(request);

        // Add override that shouldn't apply to unauthenticated
        flagClient.addOverride(created.getId(),
                FeatureFlagTestHelper.userOverride(UUID.randomUUID().toString(), false), authToken);

        Response response = flagClient.evaluateAll();

        assertOk(response);
        // Unauthenticated should see the global state (true), not the user override
        response.then()
                .body("'" + created.getKey() + "'", equalTo(true));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Evaluate single unauthenticated returns global default")
    public void evaluateSingleUnauthenticated_returnsGlobalDefault() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setEnabled(false);
        request.setRolloutPercentage(100);
        FeatureFlagResponse created = getFlagHelper().createFlag(request);

        Response response = flagClient.evaluate(created.getKey());

        assertOk(response);
        response.then()
                .body("enabled", equalTo(false));
    }

    // ==================== State Changes ====================

    @Test(groups = {TestGroup.POSITIVE},
            description = "Evaluate after toggle reflects new state")
    public void evaluateAfterToggle_reflectsNewState() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setEnabled(true);
        request.setRolloutPercentage(100);
        FeatureFlagResponse created = getFlagHelper().createFlag(request);

        // Verify initial state
        Response evalBefore = flagClient.evaluate(created.getKey());
        evalBefore.then().body("enabled", equalTo(true));

        // Toggle off
        flagClient.toggleFlag(created.getId(), FeatureFlagTestHelper.toggleRequest(false), authToken);

        // Verify changed state
        Response evalAfter = flagClient.evaluate(created.getKey());
        evalAfter.then().body("enabled", equalTo(false));
    }

    @Test(groups = {TestGroup.POSITIVE},
            description = "Evaluate with percentage rollout returns deterministic result")
    public void evaluateWithPercentageRollout_deterministicResult() {
        CreateFeatureFlagRequest request = FeatureFlagTestHelper.validCreateRequest();
        request.setEnabled(true);
        request.setRolloutPercentage(50);
        FeatureFlagResponse created = getFlagHelper().createFlag(request);

        // Same auth token should get same result each time
        Response response1 = flagClient.evaluate(created.getKey(), authToken);
        boolean result1 = response1.jsonPath().getBoolean("enabled");

        Response response2 = flagClient.evaluate(created.getKey(), authToken);
        boolean result2 = response2.jsonPath().getBoolean("enabled");

        assertOk(response1);
        assertOk(response2);
        // Deterministic: same user + same key = same result
        org.testng.Assert.assertEquals(result1, result2,
                "Percentage rollout should be deterministic for the same user");
    }
}
