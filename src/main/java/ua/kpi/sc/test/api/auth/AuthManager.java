package ua.kpi.sc.test.api.auth;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.kpi.sc.test.api.client.auth.AuthClient;
import ua.kpi.sc.test.api.client.auth.TotpClient;
import ua.kpi.sc.test.api.exception.AuthenticationException;
import ua.kpi.sc.test.api.model.auth.LoginRequest;
import ua.kpi.sc.test.api.model.auth.TotpSetupResponse;
import ua.kpi.sc.test.api.util.TotpHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthManager {

    private static final Logger log = LoggerFactory.getLogger(AuthManager.class);
    private static final ConcurrentHashMap<String, TokenInfo> tokenCache = new ConcurrentHashMap<>();
    private static final AuthClient authClient = new AuthClient();
    private static final TotpClient totpClient = new TotpClient();
    private static final long TOKEN_EXPIRY_BUFFER_SECONDS = 60;
    private static final Path TOTP_SECRET_FILE = Path.of("build", ".admin-totp-secret");
    private static volatile String totpSecret;

    private AuthManager() {}

    public static String getToken(AuthContext context) {
        if (!context.enabled()) {
            return null;
        }

        String cacheKey = context.email();
        TokenInfo existing = tokenCache.get(cacheKey);

        if (existing != null && !existing.isExpired()) {
            log.debug("Using cached token for {}", cacheKey);
            return existing.accessToken();
        }

        return tokenCache.compute(cacheKey, (key, current) -> {
            if (current != null && !current.isExpired()) {
                return current;
            }
            log.info("Acquiring new token for {}", key);
            return acquireToken(context);
        }).accessToken();
    }

    private static TokenInfo acquireToken(AuthContext context) {
        LoginRequest request = LoginRequest.builder()
                .email(context.email())
                .password(context.password())
                .build();

        Response response;
        try {
            response = authClient.login(request);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException(context.email(), e);
        }

        if (response.getStatusCode() != 200) {
            throw new AuthenticationException(context.email(),
                    response.getStatusCode(), response.getBody().asString());
        }

        String accessToken = response.getCookie("access_token");
        String refreshToken = response.getCookie("refresh_token");
        String mfaToken = response.getCookie("mfa_token");

        if ("ADMIN".equals(context.tier())) {
            // Restore TOTP secret from file if JVM restarted (e.g. auto-retry)
            if (totpSecret == null) {
                totpSecret = loadTotpSecret();
            }

            if (accessToken != null && totpSecret == null) {
                // First admin login — 2FA not yet configured. Set it up.
                log.info("Setting up TOTP 2FA for admin user {}", context.email());
                Response setup = totpClient.setupTotp(accessToken);
                TotpSetupResponse setupData = setup.as(TotpSetupResponse.class);
                totpSecret = setupData.getManualEntryKey();
                saveTotpSecret(totpSecret);

                String code = TotpHelper.generateCode(totpSecret);
                totpClient.verifySetup(accessToken, code);
                log.info("TOTP 2FA setup completed for admin user {}", context.email());

                // Re-login — now returns mfa_token instead of access_token
                Response reLogin = authClient.login(request);
                mfaToken = reLogin.getCookie("mfa_token");
                accessToken = null;
            }

            if (mfaToken != null) {
                log.info("Completing MFA verification for admin user {}", context.email());
                String code = TotpHelper.generateCode(totpSecret);
                Response verify = totpClient.verifyLogin(mfaToken, code);
                accessToken = verify.getCookie("access_token");
                refreshToken = verify.getCookie("refresh_token");
            }
        }

        if (accessToken == null) {
            throw new AuthenticationException(context.email(),
                    response.getStatusCode(), "No access_token cookie in login response");
        }
        // Default 1h expiry matching backend JWT config, minus buffer
        Instant expiresAt = Instant.now().plusSeconds(3600 - TOKEN_EXPIRY_BUFFER_SECONDS);

        return new TokenInfo(accessToken, refreshToken, expiresAt);
    }

    public static void clearCache() {
        log.info("Clearing auth token cache ({} entries)", tokenCache.size());
        tokenCache.clear();
    }

    private static String loadTotpSecret() {
        try {
            if (Files.exists(TOTP_SECRET_FILE)) {
                String secret = Files.readString(TOTP_SECRET_FILE).trim();
                if (!secret.isEmpty()) {
                    log.info("Restored TOTP secret from {}", TOTP_SECRET_FILE);
                    return secret;
                }
            }
        } catch (IOException e) {
            log.warn("Failed to load TOTP secret from file: {}", e.getMessage());
        }
        return null;
    }

    private static void saveTotpSecret(String secret) {
        try {
            Files.createDirectories(TOTP_SECRET_FILE.getParent());
            Files.writeString(TOTP_SECRET_FILE, secret);
            log.info("Saved TOTP secret to {}", TOTP_SECRET_FILE);
        } catch (IOException e) {
            log.warn("Failed to save TOTP secret to file: {}", e.getMessage());
        }
    }

    record TokenInfo(String accessToken, String refreshToken, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
