package ua.kpi.sc.test.api.tests.club;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.base.BaseAuthenticatedApiTest;
import ua.kpi.sc.test.api.config.TestGroup;

@Epic("Engagements")
@Feature("Club API")
public class ClubTest extends BaseAuthenticatedApiTest {

    @Test(enabled = false, groups = {TestGroup.POSITIVE},
            description = "Create club returns 201")
    public void createClub() {
        // TODO: Implement when /api/v1/clubs POST is ready
    }

    @Test(enabled = false, groups = {TestGroup.POSITIVE},
            description = "Get club by ID returns 200")
    public void getClubById() {
        // TODO: Implement when /api/v1/clubs/{id} GET is ready
    }

    @Test(enabled = false, groups = {TestGroup.POSITIVE},
            description = "List clubs returns paginated response")
    public void listClubs() {
        // TODO: Implement when /api/v1/clubs GET is ready
    }

    @Test(enabled = false, groups = {TestGroup.POSITIVE},
            description = "Update club returns 200")
    public void updateClub() {
        // TODO: Implement when /api/v1/clubs/{id} PUT is ready
    }

    @Test(enabled = false, groups = {TestGroup.POSITIVE},
            description = "Delete club returns 204")
    public void deleteClub() {
        // TODO: Implement when /api/v1/clubs/{id} DELETE is ready
    }

    @Test(enabled = false, groups = {TestGroup.NEGATIVE},
            description = "Create club with invalid data returns 400")
    public void createClubWithInvalidDataReturns400() {
        // TODO: Implement when /api/v1/clubs POST is ready
    }
}
