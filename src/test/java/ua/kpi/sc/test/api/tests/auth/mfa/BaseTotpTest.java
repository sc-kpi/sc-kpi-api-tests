package ua.kpi.sc.test.api.tests.auth.mfa;

import io.restassured.response.Response;
import ua.kpi.sc.test.api.client.auth.AuthClient;
import ua.kpi.sc.test.api.client.auth.TotpClient;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.auth.LoginRequest;
import ua.kpi.sc.test.api.model.auth.RegisterRequest;
import ua.kpi.sc.test.api.tests.auth.BaseAuthTest;
import ua.kpi.sc.test.api.util.TotpHelper;

import java.util.List;

public abstract class BaseTotpTest extends BaseAuthTest {

    protected final TotpClient totpClient = new TotpClient();

    protected TotpContext setupTotpForNewUser() {
        RegisterRequest regRequest = TestDataFactory.validRegisterRequest();
        Response regResponse = authClient.register(regRequest);
        assertCreated(regResponse);
        String accessToken = extractCookie(regResponse, "access_token");

        Response setupResponse = totpClient.setupTotp(accessToken);
        assertOk(setupResponse);
        String manualEntryKey = setupResponse.jsonPath().getString("manualEntryKey");

        String totpCode = TotpHelper.generateCode(manualEntryKey);
        Response verifyResponse = totpClient.verifySetup(accessToken, totpCode);
        assertOk(verifyResponse);
        List<String> recoveryCodes = verifyResponse.jsonPath().getList("codes", String.class);

        return new TotpContext(
                regRequest.getEmail(),
                regRequest.getPassword(),
                accessToken,
                manualEntryKey,
                recoveryCodes
        );
    }

    protected record TotpContext(
            String email,
            String password,
            String accessToken,
            String secret,
            List<String> recoveryCodes
    ) {}

    protected String loginAndGetMfaToken(String email, String password) {
        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();
        Response loginResponse = authClient.login(loginRequest);
        assertOk(loginResponse);
        return extractCookie(loginResponse, "mfa_token");
    }
}
