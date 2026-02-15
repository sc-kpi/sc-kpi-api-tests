package ua.kpi.sc.test.api.tests.featureflag;

import org.testng.annotations.AfterClass;
import ua.kpi.sc.test.api.base.BaseAdminApiTest;
import ua.kpi.sc.test.api.client.featureflag.FeatureFlagClient;
import ua.kpi.sc.test.api.data.FeatureFlagTestHelper;

public abstract class BaseFeatureFlagTest extends BaseAdminApiTest {

    protected final FeatureFlagClient flagClient = new FeatureFlagClient();
    protected FeatureFlagTestHelper flagHelper;

    @Override
    public void baseSetUp() {
        super.baseSetUp();
    }

    protected FeatureFlagTestHelper getFlagHelper() {
        if (flagHelper == null) {
            flagHelper = new FeatureFlagTestHelper(flagClient, authToken);
        }
        return flagHelper;
    }

    @AfterClass(alwaysRun = true)
    public void cleanupFlags() {
        if (flagHelper != null) {
            flagHelper.cleanup();
        }
    }
}
