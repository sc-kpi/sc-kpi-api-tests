package ua.kpi.sc.test.api.base;

import ua.kpi.sc.test.api.annotation.Authentication;

@Authentication(tier = "BASIC")
public abstract class BaseAuthenticatedApiTest extends BaseApiTest {
}
