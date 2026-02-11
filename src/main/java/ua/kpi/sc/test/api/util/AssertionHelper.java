package ua.kpi.sc.test.api.util;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

public final class AssertionHelper {

    private AssertionHelper() {}

    @Step("Assert status code is {expectedStatus}")
    public static void assertStatusCode(Response response, int expectedStatus) {
        assertThat(response.getStatusCode())
                .as("Expected HTTP status %d but got %d. Body: %s",
                        expectedStatus, response.getStatusCode(), response.getBody().asPrettyString())
                .isEqualTo(expectedStatus);
    }

    @Step("Assert content type contains '{expectedContentType}'")
    public static void assertContentType(Response response, String expectedContentType) {
        assertThat(response.getContentType())
                .as("Expected content type containing '%s'", expectedContentType)
                .containsIgnoringCase(expectedContentType);
    }

    @Step("Assert response time is less than {maxTimeMs}ms")
    public static void assertResponseTime(Response response, long maxTimeMs) {
        assertThat(response.getTime())
                .as("Expected response time < %dms but was %dms", maxTimeMs, response.getTime())
                .isLessThan(maxTimeMs);
    }

    @Step("Assert JSON path '{jsonPath}' equals '{expectedValue}'")
    public static void assertJsonPath(Response response, String jsonPath, Object expectedValue) {
        Object actual = response.jsonPath().get(jsonPath);
        assertThat(actual)
                .as("Expected JSON path '%s' to be '%s' but was '%s'", jsonPath, expectedValue, actual)
                .isEqualTo(expectedValue);
    }

    @Step("Assert JSON path '{jsonPath}' is not null")
    public static void assertJsonPathNotNull(Response response, String jsonPath) {
        Object actual = response.jsonPath().get(jsonPath);
        assertThat(actual)
                .as("Expected JSON path '%s' to be not null", jsonPath)
                .isNotNull();
    }

    @Step("Assert JSON path '{jsonPath}' contains '{substring}'")
    public static void assertJsonPathContains(Response response, String jsonPath, String substring) {
        String actual = response.jsonPath().getString(jsonPath);
        assertThat(actual)
                .as("Expected JSON path '%s' to contain '%s'", jsonPath, substring)
                .contains(substring);
    }

    @Step("Assert response body is not empty")
    public static void assertBodyNotEmpty(Response response) {
        assertThat(response.getBody().asString())
                .as("Expected response body to not be empty")
                .isNotEmpty();
    }

    public static void assertStatusAndContentType(Response response, int status, String contentType) {
        Allure.step("Assert status " + status + " and content type " + contentType, () -> {
            assertStatusCode(response, status);
            assertContentType(response, contentType);
        });
    }
}
