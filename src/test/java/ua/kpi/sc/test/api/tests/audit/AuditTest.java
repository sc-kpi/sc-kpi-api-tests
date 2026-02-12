package ua.kpi.sc.test.api.tests.audit;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;
import ua.kpi.sc.test.api.base.BaseAdminApiTest;
import ua.kpi.sc.test.api.config.TestGroup;

@Epic("Audit")
@Feature("Audit Log API")
public class AuditTest extends BaseAdminApiTest {

    @Test(groups = {TestGroup.TODO},
            description = "Get audit logs returns 200")
    public void getAuditLogs() {
        // TODO: Implement when /api/v1/admin/audit-logs GET is ready
    }

    @Test(groups = {TestGroup.TODO},
            description = "Filter audit logs by entity type")
    public void filterAuditLogsByEntityType() {
        // TODO: Implement when /api/v1/admin/audit-logs GET is ready
    }

    @Test(groups = {TestGroup.TODO},
            description = "Non-admin access to audit logs returns 403")
    public void nonAdminAccessReturns403() {
        // TODO: Implement when /api/v1/admin/audit-logs GET is ready
    }
}
