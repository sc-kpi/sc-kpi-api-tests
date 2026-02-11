package ua.kpi.sc.test.api.auth;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.kpi.sc.test.api.client.auth.AuthClient;
import ua.kpi.sc.test.api.exception.AuthenticationException;
import ua.kpi.sc.test.api.model.auth.LoginRequest;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthManager {

    private static final Logger log = LoggerFactory.getLogger(AuthManager.class);
    private static final ConcurrentHashMap<String, TokenInfo> tokenCache = new ConcurrentHashMap<>();
    private static final AuthClient authClient = new AuthClient();
    private static final long TOKEN_EXPIRY_BUFFER_SECONDS = 60;

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

        String accessToken = response.jsonPath().getString("accessToken");
        String refreshToken = response.jsonPath().getString("refreshToken");
        long expiresIn = response.jsonPath().getLong("expiresIn");

        Instant expiresAt = Instant.now().plusSeconds(expiresIn - TOKEN_EXPIRY_BUFFER_SECONDS);

        return new TokenInfo(accessToken, refreshToken, expiresAt);
    }

    public static void clearCache() {
        log.info("Clearing auth token cache ({} entries)", tokenCache.size());
        tokenCache.clear();
    }

    record TokenInfo(String accessToken, String refreshToken, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
