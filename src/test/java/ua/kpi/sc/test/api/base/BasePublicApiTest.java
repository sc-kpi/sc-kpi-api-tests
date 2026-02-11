package ua.kpi.sc.test.api.base;

import ua.kpi.sc.test.api.annotation.Authentication;

@Authentication(enabled = false)
public abstract class BasePublicApiTest extends BaseApiTest {
}
