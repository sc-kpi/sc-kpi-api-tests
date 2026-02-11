package ua.kpi.sc.test.api.tests.department;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.base.BaseAuthenticatedApiTest;
import ua.kpi.sc.test.api.config.TestGroup;

@Epic("Council")
@Feature("Department API")
public class DepartmentTest extends BaseAuthenticatedApiTest {

    @Test(enabled = false, groups = {TestGroup.POSITIVE},
            description = "List departments returns 200")
    public void listDepartments() {
        // TODO: Implement when /api/v1/departments GET is ready
    }

    @Test(enabled = false, groups = {TestGroup.POSITIVE},
            description = "Get department by ID returns 200")
    public void getDepartmentById() {
        // TODO: Implement when /api/v1/departments/{id} GET is ready
    }

    @Test(enabled = false, groups = {TestGroup.POSITIVE},
            description = "Get department members returns 200")
    public void getDepartmentMembers() {
        // TODO: Implement when /api/v1/departments/{id}/members GET is ready
    }

    @Test(enabled = false, groups = {TestGroup.NEGATIVE},
            description = "Get non-existent department returns 404")
    public void getNonExistentDepartmentReturns404() {
        // TODO: Implement when /api/v1/departments/{id} GET is ready
    }
}
