package ua.kpi.sc.test.api.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.kpi.sc.test.api.annotation.Authentication;
import ua.kpi.sc.test.api.config.Config;
import ua.kpi.sc.test.api.config.TestConfig;

import java.lang.reflect.Method;
import java.util.Map;

public final class AuthContextResolver {

    private static final Logger log = LoggerFactory.getLogger(AuthContextResolver.class);

    private AuthContextResolver() {}

    public static AuthContext resolve(Method method, Class<?> testClass) {
        if (!Config.isAuthEnabled()) {
            log.debug("Auth globally disabled, returning disabled context");
            return AuthContext.disabled();
        }

        Authentication methodAuth = method.getAnnotation(Authentication.class);
        Authentication classAuth = testClass.getAnnotation(Authentication.class);

        Authentication effective = methodAuth != null ? methodAuth : classAuth;

        if (effective == null || !effective.enabled()) {
            return AuthContext.disabled();
        }

        if (!effective.email().isEmpty() && !effective.password().isEmpty()) {
            log.debug("Using explicit credentials for {}", effective.email());
            return AuthContext.forCredentials(effective.email(), effective.password());
        }

        String tier = effective.tier();
        Map<String, TestConfig.TierCredentials> tierCreds = Config.auth().getTierCredentials();

        if (tierCreds != null && tierCreds.containsKey(tier)) {
            TestConfig.TierCredentials creds = tierCreds.get(tier);
            log.debug("Using tier '{}' credentials for {}", tier, creds.getEmail());
            return AuthContext.forTier(tier, creds.getEmail(), creds.getPassword());
        }

        log.warn("No credentials found for tier '{}', returning disabled context", tier);
        return AuthContext.disabled();
    }
}
