package ua.kpi.sc.test.api.base;

import io.restassured.response.Response;
import ua.kpi.sc.test.api.util.SchemaValidator;

public abstract class BaseSchemaValidationTest extends BaseApiTest {

    protected void validateSchema(Response response, String schemaPath) {
        SchemaValidator.validateSchema(response, schemaPath);
    }

    protected void validateProblemDetailSchema(Response response) {
        SchemaValidator.validateProblemDetailSchema(response);
    }

    protected void validateHealthSchema(Response response) {
        SchemaValidator.validateHealthSchema(response);
    }
}
