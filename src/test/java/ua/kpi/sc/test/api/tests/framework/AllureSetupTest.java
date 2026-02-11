package ua.kpi.sc.test.api.tests.framework;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.base.BasePublicApiTest;
import ua.kpi.sc.test.api.config.TestGroup;
import ua.kpi.sc.test.api.util.AllureHelper;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Framework")
@Feature("Allure Integration")
public class AllureSetupTest extends BasePublicApiTest {

    @Test(groups = {TestGroup.FRAMEWORK})
    public void allureStepsAreRecorded() {
        stepOne();
        stepTwo();
        stepThree();
    }

    @Step("Step 1: Initialize test context")
    private void stepOne() {
        assertThat(true).isTrue();
    }

    @Step("Step 2: Execute operation")
    private void stepTwo() {
        Allure.parameter("testParam", "testValue");
        assertThat("hello").contains("ell");
    }

    @Step("Step 3: Verify result")
    private void stepThree() {
        AllureHelper.attachText("Sample Attachment", "This is a test attachment for Allure verification");
        assertThat(42).isGreaterThan(0);
    }
}
