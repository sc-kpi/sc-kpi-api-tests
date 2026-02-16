package ua.kpi.sc.test.api.client.auth;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.model.auth.TotpDisableRequest;
import ua.kpi.sc.test.api.model.auth.TotpVerifyRequest;

public class TotpClient extends ApiClient {

    @Step("POST /auth/2fa/setup — Setup TOTP 2FA")
    public Response setupTotp(String accessToken) {
        return postEmpty(Endpoint.AUTH_2FA_SETUP, accessToken);
    }

    @Step("POST /auth/2fa/verify-setup — Verify TOTP setup with code: {code}")
    public Response verifySetup(String accessToken, String code) {
        TotpVerifyRequest request = TotpVerifyRequest.builder().code(code).build();
        return post(Endpoint.AUTH_2FA_VERIFY_SETUP, request, accessToken);
    }

    @Step("POST /auth/2fa/disable — Disable TOTP 2FA")
    public Response disableTotp(String accessToken, String password, String code) {
        TotpDisableRequest request = TotpDisableRequest.builder()
                .password(password)
                .code(code)
                .build();
        return post(Endpoint.AUTH_2FA_DISABLE, request, accessToken);
    }

    @Step("GET /auth/2fa/status — Get TOTP 2FA status")
    public Response getStatus(String accessToken) {
        return get(Endpoint.AUTH_2FA_STATUS, accessToken);
    }

    @Step("POST /auth/2fa/recovery-codes/regenerate — Regenerate recovery codes")
    public Response regenerateRecoveryCodes(String accessToken, String code) {
        TotpVerifyRequest request = TotpVerifyRequest.builder().code(code).build();
        return post(Endpoint.AUTH_2FA_RECOVERY_CODES_REGENERATE, request, accessToken);
    }

    @Step("POST /auth/2fa/verify-login — Verify login with TOTP code")
    public Response verifyLogin(String mfaToken, String code) {
        TotpVerifyRequest request = TotpVerifyRequest.builder().code(code).build();
        return postWithCookie(Endpoint.AUTH_2FA_VERIFY_LOGIN, request, "mfa_token", mfaToken);
    }

    /**
     * POST with body and cookie (not available in base ApiClient).
     */
    private Response postWithCookie(String path, Object body, String cookieName, String cookieValue) {
        return requestSpec().cookie(cookieName, cookieValue).body(body).post(path);
    }
}
