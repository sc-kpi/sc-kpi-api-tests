package ua.kpi.sc.test.api.base;

import ua.kpi.sc.test.api.annotation.Authentication;

@Authentication(tier = "ADMIN")
public abstract class BaseAdminApiTest extends BaseApiTest {
}
