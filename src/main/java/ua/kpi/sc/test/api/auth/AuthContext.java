package ua.kpi.sc.test.api.auth;

public record AuthContext(
        boolean enabled,
        String email,
        String password,
        String tier,
        String source
) {

    public static AuthContext disabled() {
        return new AuthContext(false, null, null, null, "disabled");
    }

    public static AuthContext forTier(String tier, String email, String password) {
        return new AuthContext(true, email, password, tier, "config");
    }

    public static AuthContext forCredentials(String email, String password) {
        return new AuthContext(true, email, password, null, "explicit");
    }
}
