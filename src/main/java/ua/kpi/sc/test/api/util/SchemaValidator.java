package ua.kpi.sc.test.api.util;

import io.qameta.allure.Step;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import ua.kpi.sc.test.api.exception.SchemaValidationException;

import static org.assertj.core.api.Assertions.assertThat;

public final class SchemaValidator {

    private SchemaValidator() {}

    @Step("Validate response against JSON schema: {schemaPath}")
    public static void validateSchema(Response response, String schemaPath) {
        try {
            response.then().assertThat()
                    .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/" + schemaPath));
        } catch (AssertionError e) {
            throw new SchemaValidationException(schemaPath, response.getStatusLine(),
                    response.getBody().asString(), e);
        }
    }

    @Step("Validate response against problem detail schema (RFC 9457)")
    public static void validateProblemDetailSchema(Response response) {
        validateSchema(response, "problem-detail.json");
    }

    @Step("Validate response against health response schema")
    public static void validateHealthSchema(Response response) {
        validateSchema(response, "health-response.json");
    }
}
