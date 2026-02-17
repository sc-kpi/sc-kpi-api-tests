package ua.kpi.sc.test.api.tests.ratelimit;

import org.testng.annotations.AfterClass;
import ua.kpi.sc.test.api.base.BaseAdminApiTest;
import ua.kpi.sc.test.api.client.ratelimit.RateLimitClient;
import ua.kpi.sc.test.api.data.RateLimitTestHelper;

public abstract class BaseRateLimitTest extends BaseAdminApiTest {

    protected final RateLimitClient rateLimitClient = new RateLimitClient();
    protected RateLimitTestHelper rateLimitHelper;

    @Override
    public void baseSetUp() {
        super.baseSetUp();
    }

    protected RateLimitTestHelper getRateLimitHelper() {
        if (rateLimitHelper == null) {
            rateLimitHelper = new RateLimitTestHelper(rateLimitClient, authToken);
        }
        return rateLimitHelper;
    }

    @AfterClass(alwaysRun = true)
    public void cleanupRateLimits() {
        if (rateLimitHelper != null) {
            rateLimitHelper.cleanup();
        }
    }
}
