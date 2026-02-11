package ua.kpi.sc.test.api.tests.document;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.base.BaseAuthenticatedApiTest;
import ua.kpi.sc.test.api.config.TestGroup;

@Epic("Documents")
@Feature("Document API")
public class DocumentTest extends BaseAuthenticatedApiTest {

    @Test(enabled = false, groups = {TestGroup.POSITIVE},
            description = "Create document returns 201")
    public void createDocument() {
        // TODO: Implement when /api/v1/documents POST is ready
    }

    @Test(enabled = false, groups = {TestGroup.POSITIVE},
            description = "Get document by ID returns 200")
    public void getDocumentById() {
        // TODO: Implement when /api/v1/documents/{id} GET is ready
    }

    @Test(enabled = false, groups = {TestGroup.POSITIVE},
            description = "List documents returns paginated response")
    public void listDocuments() {
        // TODO: Implement when /api/v1/documents GET is ready
    }

    @Test(enabled = false, groups = {TestGroup.POSITIVE},
            description = "Delete document returns 204")
    public void deleteDocument() {
        // TODO: Implement when /api/v1/documents/{id} DELETE is ready
    }
}
