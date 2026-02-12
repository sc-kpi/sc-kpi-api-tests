package ua.kpi.sc.test.api.tests.project;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.base.BaseAuthenticatedApiTest;
import ua.kpi.sc.test.api.config.TestGroup;

@Epic("Engagements")
@Feature("Project API")
public class ProjectTest extends BaseAuthenticatedApiTest {

    @Test(groups = {TestGroup.TODO},
            description = "Create project returns 201")
    public void createProject() {
        // TODO: Implement when /api/v1/projects POST is ready
    }

    @Test(groups = {TestGroup.TODO},
            description = "Get project by ID returns 200")
    public void getProjectById() {
        // TODO: Implement when /api/v1/projects/{id} GET is ready
    }

    @Test(groups = {TestGroup.TODO},
            description = "List projects returns paginated response")
    public void listProjects() {
        // TODO: Implement when /api/v1/projects GET is ready
    }

    @Test(groups = {TestGroup.TODO},
            description = "Update project returns 200")
    public void updateProject() {
        // TODO: Implement when /api/v1/projects/{id} PUT is ready
    }

    @Test(groups = {TestGroup.TODO},
            description = "Delete project returns 204")
    public void deleteProject() {
        // TODO: Implement when /api/v1/projects/{id} DELETE is ready
    }
}
