package ua.kpi.sc.test.api.tests.framework;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.base.BasePublicApiTest;
import ua.kpi.sc.test.api.config.Config;
import ua.kpi.sc.test.api.config.TestGroup;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Framework")
@Feature("Configuration")
public class ConfigurationTest extends BasePublicApiTest {

    @Test(groups = {TestGroup.FRAMEWORK})
    public void configurationLoadsSuccessfully() {
        assertThat(Config.get()).isNotNull();
        assertThat(Config.baseUrl()).isNotBlank();
        assertThat(Config.timeout()).isGreaterThan(0);
    }

    @Test(groups = {TestGroup.FRAMEWORK})
    public void baseUrlIsValidFormat() {
        String baseUrl = Config.baseUrl();
        assertThat(baseUrl)
                .startsWith("http")
                .doesNotEndWith("/");
    }

    @Test(groups = {TestGroup.FRAMEWORK})
    public void authConfigIsAccessible() {
        assertThat(Config.auth()).isNotNull();
        assertThat(Config.auth().getTokenEndpoint()).isNotBlank();
    }

    @Test(groups = {TestGroup.FRAMEWORK})
    public void executionConfigHasDefaults() {
        assertThat(Config.execution()).isNotNull();
        assertThat(Config.execution().getParallel()).isNotNull();
        assertThat(Config.execution().getThreadCount()).isGreaterThan(0);
    }
}
